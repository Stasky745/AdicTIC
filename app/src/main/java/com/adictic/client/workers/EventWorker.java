package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;

public class EventWorker extends Worker {
    public EventWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(!Funcions.accessibilityServiceOn(getApplicationContext()))
            return Result.failure();

        boolean start = getInputData().getBoolean("start", false);
        int events = start ? 1 : 0;

        AccessibilityScreenService.instance.setActiveEvents(events);
        AccessibilityScreenService.instance.updateDeviceBlock();

        return Result.success();
    }
}
