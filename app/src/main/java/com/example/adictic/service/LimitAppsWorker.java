package com.example.adictic.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class LimitAppsWorker extends Worker {
    public LimitAppsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String TAG = "LimitAppsWorker";

        Log.d(TAG, "Starting Worker");

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), 0, -1);

        long nextHorari = Funcions.checkHoraris(getApplicationContext()) - Calendar.getInstance().getTimeInMillis();

        List<AppUsage> listCurrentUsage = (List<AppUsage>) gul.get(0).usage;

        Map<String,Long> limitApps = TodoApp.getLimitApps();

        long nextWorker = -1;
        for(Map.Entry<String,Long> entry : limitApps.entrySet()){
            AppUsage appUsage = listCurrentUsage.get(listCurrentUsage.indexOf(entry.getKey()));
            if(appUsage.totalTime >= entry.getValue()) TodoApp.getBlockedApps().add(entry.getKey());
            else{
                if(nextWorker == -1 || entry.getValue()-appUsage.totalTime < nextWorker){
                    nextWorker = entry.getValue()-appUsage.totalTime;
                }
            }
        }

        if(nextHorari == -2) nextHorari = nextWorker+1;

        Funcions.runLimitAppsWorker(getApplicationContext(), Math.min(nextHorari, nextWorker));

        return Result.success();
    }
}
