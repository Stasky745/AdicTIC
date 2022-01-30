package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.util.hilt.Repository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.EntryPointAccessors;

public class BlockDeviceWorker extends Worker {

    AdicticRepository repository;

    public BlockDeviceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        repository = EntryPointAccessors.fromApplication(getApplicationContext(), AdicticRepository.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        if(repository.accessibilityServiceOn()){
            AccessibilityScreenService.instance.setExcessUsageDevice(true);
            AccessibilityScreenService.instance.updateDeviceBlock();
            return Result.success();
        }
        return Result.failure();
    }
}
