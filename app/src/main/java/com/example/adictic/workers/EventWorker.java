package com.example.adictic.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.service.AccessibilityScreenService;

public class EventWorker extends Worker {
    public EventWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(AccessibilityScreenService.instance == null) {
            return Result.failure();
        }

        boolean start = getInputData().getBoolean("start", false);
        int events = 0;
        if(start)
            events = 1;

        AccessibilityScreenService.instance.setActiveEvents(events);

        return Result.success();
    }
}
