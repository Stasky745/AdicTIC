package com.example.adictic.workers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.service.ForegroundService;

public class ServiceWorker extends Worker {
    public ServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isMyServiceRunning())
            return Result.success();

        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        getApplicationContext().startForegroundService(intent);

        return Result.success();
    }

    private boolean isMyServiceRunning() {
        return ForegroundService.actiu;
    }
}
