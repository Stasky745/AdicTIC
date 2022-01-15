package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;
import com.adictic.common.database.AppDatabase;
import com.adictic.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class BlockSingleAppWorker extends Worker {
    public BlockSingleAppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String pkgName = getInputData().getString("pkgName");

        if(Funcions.accessibilityServiceOn(getApplicationContext())){
            AccessibilityScreenService.instance.addBlockedApp(pkgName);
            AccessibilityScreenService.instance.updateDeviceBlock();
        }

        return Result.success();
    }

}
