package com.example.adictic.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.adictic.common.entity.GeoFill;
import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.AdicticApp;
import com.example.adictic.util.Funcions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//https://github.com/android/location-samples/blob/432d3b72b8c058f220416958b444274ddd186abd/LocationUpdatesForegroundService/app/src/main/java/com/google/android/gms/location/sample/locationupdatesforegroundservice/LocationUpdatesService.java
public class ForegroundService extends Service {
    public static boolean actiu = false;
    private final String CHANNEL_ID = "ForegroundServiceChannel";
    private final String TAG = "ForegroundService";

    private long lastUpdate = 0;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private LocationListener _locListener;
    private LocationManager _locManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        _locListener = new MyLocationListener();
        _locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            _locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, _locListener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        actiu = true;
        createNotification();
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, NavActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.app_name))
                        .setContentText(getText(R.string.service_notification_message))
                        .setSmallIcon(R.drawable.adictic_nolletra)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_message))
                        .build();

// Notification ID cannot be 0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else
            startForeground(1, notification);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                            sendLocation();
                        } else {
                            Log.w(TAG, "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setInterval(Constants.HOUR_IN_MILLIS / 2)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(Constants.HOUR_IN_MILLIS * 2);
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);
        mLocation = location;
        if(DateTime.now().getMillis() - lastUpdate > Constants.HOUR_IN_MILLIS / 2){
            sendLocation();
        }
    }

    private void sendLocation(){
        AdicticApi api = ((AdicticApp) getApplicationContext()).getAPI();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        GeoFill fill = new GeoFill();
        fill.longitud = mLocation.getLongitude();
        fill.latitud = mLocation.getLatitude();

        String CURRENT_TIME_FORMAT = "HH:mm dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(CURRENT_TIME_FORMAT, Locale.getDefault());
        fill.hora = dateFormat.format(Calendar.getInstance().getTime());

        assert sharedPreferences != null;
        Call<String> call = api.postCurrentLocation(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1), fill);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful())
                    lastUpdate = DateTime.now().getMillis();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            NotificationChannel currChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (currChannel == null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        actiu = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyLocationListener implements LocationListener {
        private final String TAG = "ForegroundService - LocationListener";
        @Override
        public void onLocationChanged(Location location)
        {
            Log.v(TAG, "location changes");
        }
    }
}
