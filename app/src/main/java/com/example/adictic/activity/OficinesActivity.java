package com.example.adictic.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.icu.text.IDNA;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.adictic.entity.Oficina;
import com.example.adictic.rest.TodoApi;

import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
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

/** https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/StarterMapFragment.java **/

public class OficinesActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private TodoApi mTodoService;

    private MapView map = null;

    private static final String TAG = OficinesActivity.class.getSimpleName();

    private Spinner SP_oficines;

    private List<Oficina> oficines = new ArrayList<>();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;// 10; // 10 metres
    private static final long MIN_TIME_FOR_UPDATES = 0;//1000*60; // 1 minut

    private GeoPoint currentLocation;
    MyLocationListener locationListener;
    LocationManager locationManager;

    MyLocationNewOverlay myLocationOverlay;

    ArrayList<Marker> markers = new ArrayList<>();

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
        SP_oficines = findViewById(R.id.SP_listOficines);

        map = (MapView) findViewById(R.id.MV_map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        mTodoService = ((TodoApp) getApplication()).getAPI();

        // Actualitzem la llista d'oficines
        Call<List<Oficina>> call = mTodoService.getOficines();
        call.enqueue(new Callback<List<Oficina>>() {
            @Override
            public void onResponse(Call<List<Oficina>> call, Response<List<Oficina>> response) {
                if (response.isSuccessful()) {
                    oficines = response.body();
                    TodoApp.setOficines(oficines);
//                    setMap();
                    askPermissionsIfNecessary();

                } else {
                    oficines = TodoApp.getOficines();
//                    setMap();
                    askPermissionsIfNecessary();
                }
            }

            @Override
            public void onFailure(Call<List<Oficina>> call, Throwable t) {
                oficines = TodoApp.getOficines();
//                setMap();
                askPermissionsIfNecessary();
            }
        });
    }

    private void askPermissionsIfNecessary(){
        requestPermissionsIfNecessary(new String[]{
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }

    @SuppressLint("MissingPermission")
    private void setMap() {
//        ActivityCompat.requestPermissions(this,new String[]{
//                // if you need to show the current location, uncomment the line below
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                // WRITE_EXTERNAL_STORAGE is required in order to show the map
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//        }, 1);

//        requestPermissionsIfNecessary(new String[]{
//                // if you need to show the current location, uncomment the line below
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                // WRITE_EXTERNAL_STORAGE is required in order to show the map
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//        });

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

        //locationManager.removeUpdates(locationListener);

        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);

//        myLocationOverlay = new MyLocationNewOverlay(map);
//        myLocationOverlay.enableMyLocation();
//        myLocationOverlay.setDrawAccuracyEnabled(true);
//        map.getOverlays().add(myLocationOverlay);

        for(Oficina oficina : oficines){
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(oficina.latitude, oficina.longitude));
            marker.setTitle(oficina.name);
            OficinaInfoWindow infoWindow = new OficinaInfoWindow(R.layout.oficina_info, map, oficina);

            marker.setInfoWindow(infoWindow);

            marker.setRelatedObject(oficina);

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    if(marker.isInfoWindowShown()) InfoWindow.closeAllInfoWindowsOn(map);
                    else{
                        int pos = markers.indexOf(marker);
                        SP_oficines.setSelection(pos);
                        marker.showInfoWindow();
                        map.getController().setCenter(setInfoWindowOffset(marker.getPosition()));
                    }

                    return true;
                }
            });

            markers.add(marker);
            map.getOverlays().add(marker);
        }


        GeoPoint startPoint;

        if(currentLocation != null){
            markers = sortListbyDistance(markers, currentLocation);
            startPoint = currentLocation;
        }
        else{
            startPoint = new GeoPoint(41.981177,2.818997); // Oficina Girona
        }

        if(!markers.isEmpty()) startPoint = markers.get(0).getPosition();

        mapController.setCenter(startPoint);
        setSpinner();
    }

    public GeoPoint setInfoWindowOffset(GeoPoint gp){
        return new GeoPoint(gp.getLatitude()+0.0025, gp.getLongitude());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (map!=null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch(requestCode){
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                boolean permissionsGranted = true;
                int i = 0;
                while(permissionsGranted && i < permissions.length){
                    if (ContextCompat.checkSelfPermission(this, permissions[i])
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        permissionsGranted = false;
                    }
                    i++;
                }

                if(permissionsGranted) setMap();
                else{
                    Toast.makeText(this, getString(R.string.need_permission), Toast.LENGTH_LONG).show();
                    this.finish();
                }
            }
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        else setMap();
    }

    private void setSpinner(){

        MarkerAdapter adapter =
                new MarkerAdapter(getApplicationContext(),
                        markers);

        SP_oficines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Marker marker = markers.get(i);
                InfoWindow.closeAllInfoWindowsOn(map);
                map.getController().setCenter(setInfoWindowOffset(marker.getPosition()));
                marker.showInfoWindow();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        SP_oficines.setAdapter(adapter);
        SP_oficines.setSelection(0);
    }

    private class MarkerAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflter;
        List<Marker> markers;

        public MarkerAdapter(@NonNull Context context, List<Marker> m) {
            this.context = context;
            inflter = (LayoutInflater.from(context));
            markers = m;
        }

        @Override
        public int getCount() {
            return markers.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Marker marker = markers.get(position);
            Oficina oficina = (Oficina) marker.getRelatedObject();

            View row = inflter.inflate(R.layout.oficina_spinner_item,null);
            TextView TV_nomOficina = (TextView)row.findViewById(R.id.TV_nomOficina);
            TextView TV_oficinaCiutat = (TextView)row.findViewById(R.id.TV_oficinaCiutat);

            TV_nomOficina.setText(oficina.name);
            String ciutat = "("+oficina.ciutat.toUpperCase()+")";
            TV_oficinaCiutat.setText(ciutat);

            return row;
        }
    }

    public static ArrayList<Marker> sortListbyDistance(ArrayList<Marker> markers, final GeoPoint location){
        Collections.sort(markers, new Comparator<Marker>() {
            @Override
            public int compare(Marker marker2, Marker marker1) {
                //
                if(getDistanceBetweenPoints(marker1.getPosition().getLatitude(),marker1.getPosition().getLongitude(),location.getLatitude(),location.getLongitude())>getDistanceBetweenPoints(marker2.getPosition().getLatitude(),marker2.getPosition().getLongitude(),location.getLatitude(),location.getLongitude())){
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return markers;
    }


    public static float getDistanceBetweenPoints(double firstLatitude, double firstLongitude, double secondLatitude, double secondLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(firstLatitude, firstLongitude, secondLatitude, secondLongitude, results);
        return results[0];
    }

    private class OficinaInfoWindow extends InfoWindow {

        protected Marker mMarkerRef;
        protected Oficina oficina;

        public OficinaInfoWindow(int layoutResId, MapView mapView, Oficina o) {
            super(layoutResId, mapView);
            oficina = o;
        }

        public void onOpen(Object item) {
            mMarkerRef = (Marker)item;

            if (mView==null) {
                Log.w(IMapView.LOGTAG, "Error trapped, OficinaInfoWindow.open, mView is null!");
                return;
            }

            TextView TV_nomOficina = (TextView)mView.findViewById(R.id.TV_nomOficina);
            TextView TV_descOficina = (TextView)mView.findViewById(R.id.TV_descOficina);
            TextView TV_addressOficina = (TextView)mView.findViewById(R.id.TV_addressOficina);
            Button BT_telfOficina = (Button)mView.findViewById(R.id.BT_telfOficina);

            TV_nomOficina.setText(oficina.name);
            TV_descOficina.setText(oficina.description);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TV_descOficina.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            String address = oficina.address + ". " + oficina.ciutat.toUpperCase();
            TV_addressOficina.setText(address);
            BT_telfOficina.setText(oficina.telf);
            BT_telfOficina.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String telf = oficina.telf.replace("[^\\d+]", "");
                    Intent intent = new Intent(ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+telf));
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onClose() { }
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

//    private void displayMyCurrentLocationOverlay() {
//        if( currentLocation != null) {
//            if( myLocationOverlay == null ) {
//                new ArrayItemizedOverlay
//                myLocationOverlay = new ArrayItemizedOverlay(myLocationMarker);
//                myCurrentLocationOverlayItem = new OverlayItem(currentLocation, "My Location", "My Location!");
//                myLocationOverlay.addItem(myCurrentLocationOverlayItem);
//                mapView.getOverlays().add(myLocationOverlay);
//            } else {
//                myCurrentLocationOverlayItem.setPoint(currentLocation);
//                myLocationOverlay.requestRedraw();
//            }
//            map.getController().setCenter(currentLocation);
//        }
//    }
}
