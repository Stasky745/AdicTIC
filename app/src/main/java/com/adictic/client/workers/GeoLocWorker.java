package com.adictic.client.workers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.callbacks.BooleanCallback;
import com.adictic.common.entity.GeoFill;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class GeoLocWorker extends ListenableWorker {

    private static final String TAG = GeoLocWorker.class.getSimpleName();
    private final Context mContext;
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint currentLocation = null;
    private AdicticApi mTodoService;
    private float accuracy = 0;

    public GeoLocWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences = Funcions.getEncryptedSharedPreferences(mContext);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (!Funcions.isBackgroundLocationPermissionOn(getApplicationContext())) {
            Log.d(TAG, "No hi ha permisos de localització");
            future.set(Result.failure());
            return future;
        }

        mTodoService = ((AdicticApp) getApplicationContext()).getAPI();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            future.set(Result.failure());
            return future;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        currentLocation = new GeoPoint(location);
                        Log.d(TAG, "Google Location OK - Enviant Localització");
                        enviarLoc(valid -> {
                            if (valid)
                                future.set(Result.success());
                            else
                                future.set(Result.failure());
                        });
                    }
                    else
                        future.set(Result.failure());
                })
                .addOnFailureListener(e -> getLocationWithoutGoogle(locationManager, valid -> {
                    if (valid)
                        future.set(Result.success());
                    else
                        future.set(Result.failure());
                }));

        return future;
    }

    @SuppressLint("MissingPermission")
    private void getLocationWithoutGoogle(LocationManager locationManager, BooleanCallback callback) {
        int iterations = 0;
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!isNetworkEnabled && !isGPSEnabled){
            callback.onDataGot(false);
            return;
        }

        MyLocationListener myLocationListener = new MyLocationListener();

        float oldAccuracy = 100;
        while(iterations <10 && (accuracy == 0 || Math.abs(oldAccuracy-accuracy) > 0.5 || currentLocation == null)) {
            if(isNetworkEnabled) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, myLocationListener);
            else locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, myLocationListener);
            iterations++;
        }

        locationManager.removeUpdates(myLocationListener);

        Location location;
        if(isNetworkEnabled){
            Log.d(TAG, "Network Enabled");
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            Log.d(TAG, "GPS Enabled");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        }

        enviarLoc(callback);
    }

    private void enviarLoc(BooleanCallback callback) {
        if(currentLocation == null) return;
        GeoFill fill = new GeoFill();
        fill.longitud = currentLocation.getLongitude();
        fill.latitud = currentLocation.getLatitude();

        String CURRENT_TIME_FORMAT = "HH:mm dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(CURRENT_TIME_FORMAT, Locale.getDefault());
        fill.hora = dateFormat.format(Calendar.getInstance().getTime());

        retrofit2.Call<String> call = mTodoService.postCurrentLocation(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), fill);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                callback.onDataGot(true);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                callback.onDataGot(true);
            }
        });

    }

    class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            accuracy = location.getAccuracy();
            currentLocation = new GeoPoint(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
