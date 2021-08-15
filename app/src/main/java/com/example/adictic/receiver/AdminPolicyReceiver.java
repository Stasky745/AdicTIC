package com.example.adictic.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AdminPolicyReceiver extends DeviceAdminReceiver {
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
        Log.i("MyDevicePolicyReciever",
                "Received: " + intent.getAction());
        super.onReceive(context, intent);
    }

}
