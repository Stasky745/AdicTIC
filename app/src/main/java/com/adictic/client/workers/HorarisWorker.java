package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;

public class HorarisWorker extends Worker {
    public HorarisWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(AccessibilityScreenService.instance == null) {
            return Result.failure();
        }

        boolean start = getInputData().getBoolean("start", false);
        AccessibilityScreenService.instance.setHorarisActius(start);

        return Result.success();
    }
}
