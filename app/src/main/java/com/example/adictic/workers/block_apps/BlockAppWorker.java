package com.example.adictic.workers.block_apps;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.entity.FreeUseApp;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockAppWorker extends Worker {
    public BlockAppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), 0, -1);
        List<AppUsage> appUsageList = new ArrayList<>(gul.get(0).usage);

        List<FreeUseApp> freeUseApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_FREE_USE_APPS,false);
        if(freeUseApps == null)
            freeUseApps = new ArrayList<>();

        List<BlockedApp> blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_BLOCKED_APPS,false);
        if(blockedApps == null || blockedApps.isEmpty())
            return Result.success();

        List<String> currentBlockedApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_CURRENT_BLOCKED_APPS,false);
        assert currentBlockedApps!=null;

        List<BlockedApp> notBlockedApps = blockedApps.stream()
                .filter(blockedApp -> !currentBlockedApps.contains(blockedApp.pkgName))
                .collect(Collectors.toList());

        long delay = Constants.TOTAL_MILLIS_IN_DAY;
        boolean canvis = false;

        for(BlockedApp blockedApp : notBlockedApps){
            if(appUsageList.stream().anyMatch(appUsage -> appUsage.app.pkgName.equals(blockedApp.pkgName))){
                AppUsage appUsage = appUsageList.stream().filter(appUsage2 -> appUsage2.app.pkgName.equals(blockedApp.pkgName)).findFirst().get();

                long freeUseUsage = 0;
                if (freeUseApps.stream().anyMatch(obj -> obj.pkgName.equals(blockedApp.pkgName))) {
                    FreeUseApp freeUseApp = freeUseApps.stream().filter(obj -> obj.pkgName.equals(blockedApp.pkgName)).findFirst().get();
                    freeUseUsage = freeUseApp.millisUsageEnd - freeUseApp.millisUsageStart;
                }

                if (appUsage.totalTime >= blockedApp.timeLimit + freeUseUsage) {
                    currentBlockedApps.add(blockedApp.pkgName);
                    canvis = true;
                }
                else{
                    long timeLeft = blockedApp.timeLimit - appUsage.totalTime;
                    if(timeLeft < delay)
                        delay = timeLeft;
                }
            }
        }

        if(canvis)
            Funcions.write2File(getApplicationContext(),currentBlockedApps);

        Funcions.runBlockAppsWorker(getApplicationContext(),delay);

        return Result.success();
    }
}
