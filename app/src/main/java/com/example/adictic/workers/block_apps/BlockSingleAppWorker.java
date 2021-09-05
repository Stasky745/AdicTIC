package com.example.adictic.workers.block_apps;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.common.util.Constants;
import com.example.adictic.util.Funcions;

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
        List<String> currentBlockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS,false);

        if(currentBlockedApps == null)
            currentBlockedApps = new ArrayList<>();

        currentBlockedApps.add(pkgName);

        Funcions.write2File(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, currentBlockedApps);

        return Result.success();
    }
}
