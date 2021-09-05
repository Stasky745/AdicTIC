package com.example.adictic.workers.block_apps;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.common.util.Constants;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.util.Funcions;

import java.util.List;
import java.util.stream.Collectors;

public class RestartBlockedApps extends Worker {

    public RestartBlockedApps(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String TAG = "LimitAppsWorker";

        Log.d(TAG, "Starting Worker");

        List<BlockedApp> blockedApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_LIMITED_APPS,false);

        // Cancelem tots els workers que hi pugui haver
        WorkManager.getInstance(getApplicationContext())
                .cancelAllWorkByTag(Constants.WORKER_TAG_BLOCK_APPS);

        // Afegim les apps que estan bloquejades permanentment i cap altra
        List<String> permanentBlockedApps;
        if(blockedApps == null || blockedApps.isEmpty()) {
            Log.d(TAG,"BlockedApps==NULL/Empty");
            Funcions.write2File(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, null);
            return Result.success();
        }

        permanentBlockedApps = blockedApps.stream()
                .filter(blockedApp -> blockedApp.timeLimit == 0)
                .map(blockedApp -> blockedApp.pkgName)
                .collect(Collectors.toList());

        Funcions.write2File(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, permanentBlockedApps);

        Funcions.runBlockAppsWorker(getApplicationContext(),0);

        return Result.success();
    }
}
