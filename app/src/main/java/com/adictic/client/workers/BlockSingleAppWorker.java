package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.EntryPointAccessors;

public class BlockSingleAppWorker extends Worker {

    AdicticRepository repository;

    public BlockSingleAppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        repository = EntryPointAccessors.fromApplication(getApplicationContext(), AdicticRepository.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        String pkgName = getInputData().getString("pkgName");

        if(repository.accessibilityServiceOn()){
            AccessibilityScreenService.instance.addBlockedApp(pkgName);
            AccessibilityScreenService.instance.updateDeviceBlock();
        }

        return Result.success();
    }

}
