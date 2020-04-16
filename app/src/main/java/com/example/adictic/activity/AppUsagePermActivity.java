package com.example.adictic.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.example.adictic.R;
import com.example.adictic.util.Funcions;

public class AppUsagePermActivity extends Activity {

    protected static final int REQUEST_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_usage_perm_info);

        Button bt_okay = (Button)findViewById(R.id.BT_okAppUsagePerm);

        bt_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("IEEEPA");
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                AppUsagePermActivity.this.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });
    }

    @Override
    protected void onResume() {

        if(Funcions.isAppUsagePermissionOn(this)){
            if (!Funcions.isAccessibilitySettingsOn(this)) {
                this.startActivity(new Intent(this, AccessibilityPermActivity.class));
                this.finish();
            } else {
                this.startActivity(new Intent(this, MainActivityChild.class));
                this.finish();
            }
        }

        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(Funcions.isAppUsagePermissionOn(this)){
                System.out.println("DINS");
                if (!Funcions.isAccessibilitySettingsOn(this)) {
                    this.startActivity(new Intent(this, AccessibilityPermActivity.class));
                    this.finish();
                } else {
                    this.startActivity(new Intent(this, MainActivityChild.class));
                    this.finish();
                }
            }
        }
    }

}
