package com.example.adictic.workers.horaris_workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.common.util.Constants;
import com.example.adictic.util.Funcions;

public class DespertarWorker extends Worker {
    public DespertarWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT,false).apply();
        return Result.success();
    }
}
