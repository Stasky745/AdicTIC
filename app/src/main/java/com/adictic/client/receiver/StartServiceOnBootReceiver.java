package com.adictic.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StartServiceOnBootReceiver extends BroadcastReceiver {

    @Inject
    AdicticRepository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        repository.startServiceWorker();
    }
}
