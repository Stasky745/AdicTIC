package com.example.adictic.ui.permisos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.R;
import com.example.adictic.service.AppUsageWorker;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AppUsagePermActivity extends Activity {
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_usage_perm_info);
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        Button bt_okay = findViewById(R.id.BT_okAppUsagePerm);

        bt_okay.setOnClickListener(v -> AppUsagePermActivity.this.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
    }

    @Override
    protected void onResume() {

        if (Funcions.isAppUsagePermissionOn(this)) {

            Funcions.startAppUsageWorker(getApplicationContext());

            if (!Funcions.isAdminPermissionsOn(this))
                this.startActivity(new Intent(this, DevicePolicyAdmin.class));
            if (!Funcions.isAccessibilitySettingsOn(this)) {
                this.startActivity(new Intent(this, AccessibilityPermActivity.class));
            } else {
                this.startActivity(new Intent(this, NavActivity.class));
            }
            this.finish();
        }

        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (Funcions.isAppUsagePermissionOn(this)) {
                System.out.println("DINS");
                if (!Funcions.isAccessibilitySettingsOn(this)) {
                    this.startActivity(new Intent(this, AccessibilityPermActivity.class));
                } else {
                    this.startActivity(new Intent(this, NavActivity.class));
                }
                this.finish();
            }
        }
    }

}
