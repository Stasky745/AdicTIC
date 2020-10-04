package com.example.adictic.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Oficina;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * MARKER INFO: https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/gms/java/com/example/mapdemo/MarkerDemoActivity.java
 */

public class OficinesActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private static final int REQUEST_LOCATION = 1;

    private static final String TAG = OficinesActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    private Spinner SP_oficines;

    private List<Oficina> oficines = new ArrayList<>();

    private LocationManager locationManager;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    ArrayList<Marker> markers = new ArrayList<>();

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    // [END maps_current_place_state_keys]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.
//        if(savedInstanceState != null){
//            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
//        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        setContentView(R.layout.oficines_layout);
        oficines = TodoApp.getOficines();
        SP_oficines = findViewById(R.id.SP_listOficines);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.FR_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

//        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//            public View getInfoWindow(Marker marker) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//
//                final Oficina oficina = (Oficina) marker.getTag();
//
//                View infoWindow = getLayoutInflater().inflate(R.layout.oficina_info,
//                        (FrameLayout) findViewById(R.id.FR_map),false);
//
//                TextView nomOficina = infoWindow.findViewById(R.id.TV_nomOficina);
//                nomOficina.setText(marker.getTitle());
//
//                TextView descOficina = infoWindow.findViewById(R.id.TV_descOficina);
//                descOficina.setText(oficina.desc);
//
//                TextView addressOficina = infoWindow.findViewById(R.id.TV_addressOficina);
//                String address = oficina.address + ". " + oficina.ciutat.toUpperCase();
//                addressOficina.setText(address);
//
//                Button telfOficina = infoWindow.findViewById(R.id.BT_telfOficina);
//                telfOficina.setText(oficina.telf);
//                telfOficina.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Uri number = Uri.parse("tel:" + oficina.telf);
//                        Intent dial = new Intent(Intent.ACTION_DIAL,number);
//                        startActivity(dial);
//                    }
//                });
//
//                return infoWindow;
//            }
//        });
//
//        for(Oficina o : oficines){
//            Marker marker = map.addMarker(new MarkerOptions()
//                    .position(new LatLng(o.latitude,o.longitude))
//                    .title(o.name)
//                    .snippet(o.address)
//                    //.flat(true) // marker 3D
//                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // IMATGE
//                    );
//            marker.setTag(o);
//
//            markers.add(marker);
//        }
//
//        double longitude = 2.821083;
//        double latitude = 41.980421;
//
//        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))  getLocation(longitude,latitude);
//        else OnGPS();
//
//        // Ordenar markers per proximitat
//        markers = sortListbyDistance(markers, new LatLng(latitude,longitude));
//
//        setSpinner();
//
//        //map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude))); // Descomentar per començar a la posició de l'usuari
//        map.setOnMarkerClickListener(this);
    }

    private void setSpinner(){

        ArrayAdapter<Oficina> adapter =
                new ArrayAdapter<Oficina>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item,oficines);

        SP_oficines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Oficina oficina = (Oficina) SP_oficines.getSelectedItem();
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(oficina.latitude,oficina.longitude)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        SP_oficines.setSelection(0);
    }

    public static ArrayList<Marker> sortListbyDistance(ArrayList<Marker> markers, final LatLng location){
        Collections.sort(markers, new Comparator<Marker>() {
            @Override
            public int compare(Marker marker2, Marker marker1) {
                //
                if(getDistanceBetweenPoints(marker1.getPosition().latitude,marker1.getPosition().longitude,location.latitude,location.longitude)>getDistanceBetweenPoints(marker2.getPosition().latitude,marker2.getPosition().longitude,location.latitude,location.longitude)){
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

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation(double longitude, double latitude){
        if (ActivityCompat.checkSelfPermission(
                OficinesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                OficinesActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                latitude = locationGPS.getLatitude();
                longitude = locationGPS.getLongitude();
            } else {
                Toast.makeText(this, getString(R.string.unable_location), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

}
