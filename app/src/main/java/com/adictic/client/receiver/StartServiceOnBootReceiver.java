package com.adictic.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adictic.client.service.ForegroundService;

public class StartServiceOnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(context, ForegroundService.class);
        Log.i("StartServiceOnBootReceiver", "Starting service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(it);
        else
            context.startService(it);
    }
}
