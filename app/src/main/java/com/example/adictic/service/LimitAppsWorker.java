package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LimitAppsWorker extends Worker {
    public LimitAppsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return null;
    }
}
