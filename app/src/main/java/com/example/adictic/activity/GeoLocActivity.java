package com.example.adictic.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.GeoFill;
import com.example.adictic.entity.Oficina;
import com.example.adictic.rest.TodoApi;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.ACTION_DIAL;

public class GeoLocActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private TodoApi mTodoService;

    private MapView map = null;

    private static final String TAG = GeoLocActivity.class.getSimpleName();

    private Spinner SP_fills;

    private List<GeoFill> fills = new ArrayList<>();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;// 10; // 10 metres
    private static final long MIN_TIME_FOR_UPDATES = 0;//1000*60; // 1 minut

    private GeoPoint currentLocation;
    MyLocationListener locationListener;
    LocationManager locationManager;

    MyLocationNewOverlay myLocationOverlay;

    ArrayList<Marker> markers = new ArrayList<>();


    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10*1000; // Delay for 10 seconds

    @Override
    public void onResume() {
        super.onResume();

        postGeolocActive(true);

        if (map!=null) {
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    demanarLocFills();

                    handler.postDelayed(runnable, delay);
                }
            }, delay);

            map.onResume();
        }
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible

        postGeolocActive(false);

        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void postGeolocActive(final boolean b){
        Call<String> call = mTodoService.postGeolocActive(b);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                    postGeolocActive(b);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                postGeolocActive(b);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        try{
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        }catch(java.lang.NullPointerException exc){}
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.oficines_layout);
        SP_fills = findViewById(R.id.SP_listOficines);

        map = (MapView) findViewById(R.id.MV_map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        mTodoService = ((TodoApp) getApplication()).getAPI();

        demanarLocFills();
    }

    public void demanarLocFills(){
        // Actualitzem la llista de fills
        Call<List<GeoFill>> call = mTodoService.getGeoLoc();
        call.enqueue(new Callback<List<GeoFill>>() {
            @Override
            public void onResponse(Call<List<GeoFill>> call, Response<List<GeoFill>> response) {
                if (response.isSuccessful()) {
                    fills = response.body();
                    TodoApp.setGeoFills(fills);
                    setMap();

                } else {
                    fills = TodoApp.getGeoFills();
                    Toast.makeText(getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT);
                    if(fills.get(0) != null) setMap();
                }
            }

            @Override
            public void onFailure(Call<List<GeoFill>> call, Throwable t) {
                fills = TodoApp.getGeoFills();
                Toast.makeText(getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT);
                if(fills.get(0) != null) setMap();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setMap(){
        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isNetworkEnabled){
//            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,null);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_FOR_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);

            Log.d(TAG,"Network Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        }
        else if(isGPSEnabled){
//            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,null);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_FOR_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);

            Log.d(TAG,"GPS Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        }

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()),map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);

        for(GeoFill fill : fills){
            if(fill != null) {
                Marker marker = new Marker(map);
                if (fill.latitud != null && fill.longitud != null)
                    marker.setPosition(new GeoPoint(fill.latitud, fill.longitud));
                marker.setTitle(fill.nom);
                marker.setSubDescription(fill.hora);

                marker.setRelatedObject(fill);

                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        if (marker.isInfoWindowShown()) InfoWindow.closeAllInfoWindowsOn(map);
                        else {
                            int pos = markers.indexOf(marker);
                            SP_fills.setSelection(pos);
                            marker.showInfoWindow();
                            map.getController().setCenter(setInfoWindowOffset(marker.getPosition()));
                        }

                        return true;
                    }
                });

                markers.add(marker);
                map.getOverlays().add(marker);
            }
        }

        GeoPoint startPoint;

        if(currentLocation != null){
            startPoint = currentLocation;
        }
        else if(!fills.isEmpty()){
            startPoint = new GeoPoint(fills.get(0).latitud,fills.get(0).longitud);
        }
        else{
            startPoint = new GeoPoint(41.981177,2.818997); // Oficina Girona
        }

        mapController.setCenter(startPoint);
        setSpinner();
    }

    private void setSpinner(){
        ArrayAdapter<Marker> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,markers);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        SP_fills.setAdapter(adapter);
    }

    public GeoPoint setInfoWindowOffset(GeoPoint gp){
        return new GeoPoint(gp.getLatitude()+0.0025, gp.getLongitude());
    }

    class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            currentLocation = new GeoPoint(location);
            //displayMyCurrentLocationOverlay();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
