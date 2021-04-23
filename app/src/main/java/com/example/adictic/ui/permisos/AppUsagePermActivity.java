package com.example.adictic.ui.permisos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.R;
import com.example.adictic.service.AppUsageWorker;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.util.Calendar;

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

            Calendar cal = Calendar.getInstance();
            // Agafem dades dels últims X dies per inicialitzar dades al servidor
            cal.add(Calendar.DAY_OF_YEAR, -6);
            sharedPreferences.edit().putInt("dayOfYear",cal.get(Calendar.DAY_OF_YEAR)).apply();

            OneTimeWorkRequest myWork =
                    new OneTimeWorkRequest.Builder(AppUsageWorker.class).build();

            WorkManager.getInstance(this).enqueue(myWork);

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
