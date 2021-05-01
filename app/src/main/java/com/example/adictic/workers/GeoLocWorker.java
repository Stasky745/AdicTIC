package com.example.adictic.workers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.GeoFill;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeoLocWorker extends Worker {

    private static final String TAG = GeoLocWorker.class.getSimpleName();
    private final Context mContext;
    private SharedPreferences sharedPreferences;
    private final FusedLocationProviderClient fusedLocationClient;
    private GeoPoint currentLocation = null;
    private TodoApi mTodoService;
    float accuracy = 0;

    public GeoLocWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mContext = context;
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        sharedPreferences = Funcions.getEncryptedSharedPreferences(mContext);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mTodoService = ((TodoApp) getApplicationContext()).getAPI();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) mContext, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        currentLocation = new GeoPoint(location);
                    }
                });

        if (currentLocation != null) {
            enviarLoc();
            return Result.success();
        } else if (isNetworkEnabled) {
            MyLocationListener myLocationListener = new MyLocationListener();

            float oldAccuracy = 100;
            while(accuracy == 0 || Math.abs(oldAccuracy-accuracy) > 0.5 || currentLocation == null)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, myLocationListener);

            locationManager.removeUpdates(myLocationListener);

            Log.d(TAG, "Network Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            enviarLoc();
            return Result.success();
        } else if (isGPSEnabled) {
            MyLocationListener myLocationListener = new MyLocationListener();

            float oldAccuracy = 100;
            while(accuracy == 0 || Math.abs(oldAccuracy-accuracy) > 0.5 || currentLocation == null)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, myLocationListener);

            locationManager.removeUpdates(myLocationListener);

            Log.d(TAG, "GPS Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            enviarLoc();
            return Result.success();
        } else return Result.failure();
    }

    private void enviarLoc() {
        GeoFill fill = new GeoFill();
        fill.longitud = currentLocation.getLongitude();
        fill.latitud = currentLocation.getLatitude();

        String CURRENT_TIME_FORMAT = "HH:mm dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(CURRENT_TIME_FORMAT, Locale.getDefault());
        fill.hora = dateFormat.format(Calendar.getInstance().getTime());

        retrofit2.Call<String> call = mTodoService.postCurrentLocation(sharedPreferences.getLong("idUser",-1), fill);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            }
        });

    }

    class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            accuracy = location.getAccuracy();
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
