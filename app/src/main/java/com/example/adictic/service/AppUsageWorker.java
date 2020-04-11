package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AppUsageWorker extends Worker {

    public AppUsageWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {




        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
}
