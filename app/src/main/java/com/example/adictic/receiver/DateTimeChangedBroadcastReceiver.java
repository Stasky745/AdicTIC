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
            // Inicialitzem workers de bloquejar apps
            Funcions.fetchAppBlockFromServer(context);
//            Funcions.runRestartBlockedAppsWorkerOnce(context,0);
//            Funcions.startRestartBlockedAppsWorker24h(context);

            // Inicialitzem workers d'events
            Funcions.checkEvents(context);
//            Funcions.runRestartEventsWorkerOnce(context,0);
//            Funcions.startRestartEventsWorker24h(context);

            // Inicialitzem workers d'horaris
            Funcions.checkHoraris(context);
//            Funcions.runRestartHorarisWorkerOnce(context, 0);
//            Funcions.startRestartHorarisWorker24h(context);
        }
    }
}
