package com.adictic.client.workers.event_workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.util.Funcions;
import com.adictic.common.util.Constants;

public class FinishBlockEventWorker extends Worker {
    public FinishBlockEventWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        assert sharedPreferences != null;
        int activeEvents = sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,0);
        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, activeEvents - 1).apply();

        return Result.success();
    }
}
