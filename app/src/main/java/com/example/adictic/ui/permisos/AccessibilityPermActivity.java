package com.example.adictic.ui.permisos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.R;
import com.example.adictic.service.FetchEventsWorker;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AccessibilityPermActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accessibility_perm_info);

        Button bt_okay = findViewById(R.id.BT_okAccessibilityPerm);

        startFetchEventsWorker();

        if (Funcions.isAccessibilitySettingsOn(this)) {
            this.startActivity(new Intent(this, NavActivity.class));
            this.finish();
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
                new PeriodicWorkRequest.Builder(FetchEventsWorker.class,24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("fetchEvents", ExistingPeriodicWorkPolicy.REPLACE, fetchEventsRequest);
    }

    @Override
    protected void onResume() {
        if (Funcions.isAccessibilitySettingsOn(this)) {
            this.startActivity(new Intent(this, NavActivity.class));
            this.finish();
        }

        super.onResume();
    }

    @Override
    protected void onPostResume() {
        if (Funcions.isAccessibilitySettingsOn(this)) {
            this.startActivity(new Intent(this, NavActivity.class));
            this.finish();
        }
        super.onPostResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            if (Funcions.isAccessibilitySettingsOn(this)) {
                this.startActivity(new Intent(this, NavActivity.class));
                this.finish();
            }
        }
    }
}
