package com.example.adictic.ui.permisos;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

public class DevicePolicyAdmin extends AppCompatActivity {

    protected static final int REQUEST_ENABLE = 1;
    private final static String LOG_TAG = "DevicePolicyAdmin";
    ComponentName mDPAdmin;
    DevicePolicyManager mDPM;
    private Button bt_okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_perm_info);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDPAdmin = new ComponentName(this,
                MyDevicePolicyReceiver.class);

        Log.d(LOG_TAG,"mDPM: " + mDPM);
        Log.d(LOG_TAG,"mDPAdmin: " + mDPAdmin);

        bt_okay = findViewById(R.id.BT_okAdminPerm);

        bt_okay.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mDPAdmin);
            intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_pem_intent));

            startActivityForResult(intent, REQUEST_ENABLE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bt_okay = findViewById(R.id.BT_okAdminPerm);

        bt_okay.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mDPAdmin);
            intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_pem_intent));

            startActivityForResult(intent, REQUEST_ENABLE);
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE) {
                if (!Funcions.isAccessibilitySettingsOn(this))
                    this.startActivity(new Intent(this, AccessibilityPermActivity.class));
                else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                    this.startActivity(new Intent(this,BackgroundLocationPerm.class));
                else
                    this.startActivity(new Intent(this, NavActivity.class));

                this.finish();
            }
        }
    }

    public static class MyDevicePolicyReceiver extends DeviceAdminReceiver {
        @Override
        public void onDisabled(Context context, Intent intent) {
            Toast.makeText(context, "AdicTIC's Device Admin Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            Toast.makeText(context, "AdicTIC's Device Admin is now enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return "Requesting to disable Device Admin";
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,
                    "MyDevicePolicyReciever Received: " + intent.getAction());
            super.onReceive(context, intent);
        }
    }
}
