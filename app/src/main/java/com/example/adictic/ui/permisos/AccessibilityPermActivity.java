package com.example.adictic.ui.permisos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.R;
import com.example.adictic.workers.event_workers.RestartEventsWorker;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AccessibilityPermActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accessibility_perm_info);

        Button bt_okay = findViewById(R.id.BT_okAccessibilityPerm);

        startFetchEventsWorker();

        if (Funcions.isAccessibilitySettingsOn(this)) {
            // estem a MIUI i Android v > O
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Funcions.isXiaomi()){
                checkDrawOverlayPermission();
            }
            else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                this.startActivity(new Intent(this,BackgroundLocationPerm.class));
            else{
                this.startActivity(new Intent(this, NavActivity.class));
                this.finish();
            }
        }

        bt_okay.setOnClickListener(v -> AccessibilityPermActivity.this.startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1));
    }

    protected void startFetchEventsWorker(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,5);

        long delay = cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        PeriodicWorkRequest fetchEventsRequest =
                new PeriodicWorkRequest.Builder(RestartEventsWorker.class,24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("fetchEvents", ExistingPeriodicWorkPolicy.REPLACE, fetchEventsRequest);
    }

    @Override
    protected void onResume() {
        if (Funcions.isAccessibilitySettingsOn(this)) {
            if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                this.startActivity(new Intent(this,BackgroundLocationPerm.class));
            else
                this.startActivity(new Intent(this, NavActivity.class));
            this.finish();
        }

        super.onResume();
    }

    @Override
    protected void onPostResume() {
        if (Funcions.isAccessibilitySettingsOn(this)) {
            // estem a MIUI i Android v > O
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Funcions.isXiaomi()){
                checkDrawOverlayPermission();
            }
            else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                this.startActivity(new Intent(this,BackgroundLocationPerm.class));
            else{
                this.startActivity(new Intent(this, NavActivity.class));
                this.finish();
            }
        }
        super.onPostResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            if (Funcions.isAccessibilitySettingsOn(this)) {
                // estem a MIUI i Android v > O
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Funcions.isXiaomi()){
                    checkDrawOverlayPermission();
                }
                else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                    this.startActivity(new Intent(this,BackgroundLocationPerm.class));
                else{
                    this.startActivity(new Intent(this, NavActivity.class));
                    this.finish();
                }
            }
        }
        if (resultCode == 10101){
            checkDrawOverlayPermission();
        }
    }

    private void checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (!Settings.canDrawOverlays(this)) {

            // If not, form up an Intent to launch the permission request
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));

            // Launch Intent, with the supplied request code
            startActivityForResult(intent, 10101);
        }
        else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
            this.startActivity(new Intent(this,BackgroundLocationPerm.class));
        else{
            this.startActivity(new Intent(this, NavActivity.class));
            this.finish();
        }
    }

}
