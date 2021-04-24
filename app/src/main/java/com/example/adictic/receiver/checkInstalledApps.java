package com.example.adictic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class checkInstalledApps extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            String pkgUID = intent.getDataString();
        }
    }
}
