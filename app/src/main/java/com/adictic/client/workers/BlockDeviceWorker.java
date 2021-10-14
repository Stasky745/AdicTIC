package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;

public class BlockDeviceWorker extends Worker {
    public BlockDeviceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(Funcions.accessibilityServiceOn()){
            AccessibilityScreenService.instance.setExcessUsageDevice(true);
            AccessibilityScreenService.instance.updateDeviceBlock();
            return Result.success();
        }
        return Result.failure();
    }
}
