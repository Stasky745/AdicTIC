package com.adictic.client.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.adictic.client.R;
import com.adictic.client.receiver.checkInstalledApps;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.main.NavActivity;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.GeoFill;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.MyNotificationManager;
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
import retrofit2.Response;

//https://github.com/android/location-samples/blob/432d3b72b8c058f220416958b444274ddd186abd/LocationUpdatesForegroundService/app/src/main/java/com/google/android/gms/location/sample/locationupdatesforegroundservice/LocationUpdatesService.java
//https://robertohuertas.com/2019/06/29/android_foreground_services/
public class ForegroundService extends Service {
    public static boolean actiu = false;
    PowerManager.WakeLock wakeLock = null;
    private final String CHANNEL_ID = "ForegroundServiceChannel";
    private final String TAG = "ForegroundService";

    private long lastUpdate = 0;

    private Location mLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private BroadcastReceiver checkInstalledAppsReceiver = null;

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        actiu = false;
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        intent.setPackage(getPackageName());

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ForegroundService.this);
        assert sharedPreferences != null;

        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
            stopSelf();

        registerInstallApps();
        startLocationReceiver();
    }

    private void startLocationReceiver() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.i(TAG, "LocationChanged");
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        LocationListener _locListener = new MyLocationListener();
        LocationManager _locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            _locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, _locListener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notification = createNotification();

        if (notification == null)
            stopSelf();

        // Notification ID cannot be 0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else
            startForeground(1, notification);

        actiu = true;
//        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ForegroundService::wakelock");
//        wakeLock.acquire();

        return START_STICKY;
    }

    private void registerInstallApps() {
        if(checkInstalledAppsReceiver != null)
            return;

        // Comencem el receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        checkInstalledAppsReceiver = new checkInstalledApps();
        registerReceiver(checkInstalledAppsReceiver, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
//        ClientNotificationManager clientNotificationManager = ((AdicticApp) getApplicationContext()).getNotificationManager();
//        clientNotificationManager.displayGeneralNotification(getString(R.string.app_name), getString(R.string.service_notification_message), MainActivityAbstractClass.class, MyNotificationManager.Channels.FOREGROUND_SERVICE, MyNotificationManager.NOTIF_ID_FOREGROUND_SERVICE);
        MyNotificationManager.Channel foreground_channel = MyNotificationManager.channel_info.get(MyNotificationManager.Channels.FOREGROUND_SERVICE);
        if(foreground_channel==null)
            return null;
        createNotificationChannel(foreground_channel);

        Intent notificationIntent = new Intent(this, NavActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return
                new Notification.Builder(this, foreground_channel.id)
                        .setContentTitle(getText(R.string.app_name))
                        .setContentText(getText(R.string.service_notification_message))
                        .setSmallIcon(R.drawable.adictic_nolletra)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.service_notification_message))
                        .build();

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
        /**
         * Contains parameters used by {@link FusedLocationProviderClient}.
         */
        LocationRequest mLocationRequest = LocationRequest.create()
                .setInterval(Constants.HOUR_IN_MILLIS / 2)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(Constants.HOUR_IN_MILLIS * 2);
    }

    private void onNewLocation(Location location) {
        mLocation = location;
        if(DateTime.now().getMillis() - lastUpdate > Constants.HOUR_IN_MILLIS / 2){
            Log.i(TAG, "Enviem nova localització: " + location);
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
                    super.onResponse(call, response);
                if (response.isSuccessful())
                    lastUpdate = DateTime.now().getMillis();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);

            }
        });
    }

    private void createNotificationChannel(MyNotificationManager.Channel foreground_channel) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(foreground_channel.id, foreground_channel.name, foreground_channel.notif_importance);
            channel.setDescription(foreground_channel.description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        actiu = false;

        unregisterReceiver(checkInstalledAppsReceiver);

        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        intent.setPackage(getPackageName());

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent);
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
            Log.i(TAG, "LocationChanged");
            onNewLocation(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }
}
