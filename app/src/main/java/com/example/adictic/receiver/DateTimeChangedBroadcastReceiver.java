package com.example.adictic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.adictic.util.Funcions;

public class DateTimeChangedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
            Funcions.runRestartBlockedAppsWorkerOnce(context,0);
            Funcions.runRestartEventsWorkerOnce(context,0);
        }
    }
}
