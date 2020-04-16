package com.example.adictic.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.adictic.R;
import com.example.adictic.util.Funcions;

public class DevicePolicyAdmin extends Activity {

    private final static String LOG_TAG = "DevicePolicyAdmin";
    private Button bt_okay;
    protected static final int REQUEST_ENABLE = 1;

    ComponentName mDPAdmin;
    DevicePolicyManager mDPM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_perm_info);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDPAdmin = new ComponentName(this,
                MyDevicePolicyReceiver.class);

        System.out.println("mDPM: " + mDPM);
        System.out.println("mDPAdmin: " + mDPAdmin);

        bt_okay = (Button)findViewById(R.id.BT_okAdminPerm);

        bt_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        mDPAdmin);
                intent.putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.admin_pem_intent));

                startActivityForResult(intent, REQUEST_ENABLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bt_okay = (Button)findViewById(R.id.BT_okAdminPerm);

        bt_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        mDPAdmin);
                intent.putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.admin_pem_intent));

                startActivityForResult(intent, REQUEST_ENABLE);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode){
                case REQUEST_ENABLE:
                    if(!Funcions.isAppUsagePermissionOn(this)){
                        this.startActivity(new Intent(this, AppUsagePermActivity.class));
                        this.finish();
                    }
                    else if(!Funcions.isAccessibilitySettingsOn(this)){
                        this.startActivity(new Intent(this, AccessibilityPermActivity.class));
                        this.finish();
                    }
                    else {
                        this.startActivity(new Intent(this, MainActivityChild.class));
                        this.finish();
                    }
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
            CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
            return disableRequestedSeq;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,
                    "MyDevicePolicyReciever Received: " + intent.getAction());
            super.onReceive(context, intent);
        }
    }
}
