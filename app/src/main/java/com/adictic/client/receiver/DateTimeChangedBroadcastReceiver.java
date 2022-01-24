package com.adictic.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DateTimeChangedBroadcastReceiver extends BroadcastReceiver {

    @Inject
    AdicticRepository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
            // Inicialitzem workers de bloquejar apps
            repository.fetchAppBlockFromServer();

            // Inicialitzem workers d'events
            repository.checkEvents();

            // Inicialitzem workers d'horaris
            repository.checkHoraris();
//            Funcions.runRestartHorarisWorkerOnce(context, 0);
//            Funcions.startHorarisEventsManagerWorker(context);
        }
    }
}
