package com.example.adictic.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.entity.FreeUseApp;
import com.example.adictic.entity.HorarisNit;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class LimitAppsWorker extends Worker {
    int TOTAL_MILLIS_IN_DAY = 86400000;

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

        // Agafem una llista amb les apps que falten per bloquejar
        List<BlockedApp> blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_BLOCKED_APPS,false);
        List<FreeUseApp> freeUseApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_FREE_USE_APPS,false);

        //checkHoraris();

        List<AppUsage> listCurrentUsage = (List<AppUsage>) gul.get(0).usage;

        long delayNextLimit = Long.MAX_VALUE;

        boolean canvis = false;

        for(BlockedApp app : blockedApps){
            if(!app.blockedNow) {
                AppUsage appUsage = listCurrentUsage.get(listCurrentUsage.indexOf(app.pkgName));

                // tenim en compte el temps que l'app ha estat utilitzada durant FreeUse
                long freeUseUsage = 0;
                if (freeUseApps.contains(app.pkgName))
                    freeUseUsage = freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageEnd - freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageStart;
                if (appUsage.totalTime >= app.timeLimit + freeUseUsage) {
                    app.blockedNow = true;
                    canvis = true;
                } else {
                    long timeLeft = app.timeLimit - appUsage.totalTime;
                    if (timeLeft < delayNextLimit) delayNextLimit = timeLeft;
                }
            }
            // Si ha canviat de dia i l'app consta com a bloquejada
            else if(app.blockedNow && app.timeLimit > -1){
                AppUsage appUsage = listCurrentUsage.get(listCurrentUsage.indexOf(app.pkgName));

                // tenim en compte el temps que l'app ha estat utilitzada durant FreeUse
                long freeUseUsage = 0;
                if (freeUseApps.contains(app.pkgName))
                    freeUseUsage = freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageEnd - freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageStart;
                if (appUsage.totalTime < app.timeLimit + freeUseUsage) {
                    app.blockedNow = false;
                    canvis = true;

                    long timeLeft = app.timeLimit + freeUseUsage - appUsage.totalTime;
                    if (timeLeft < delayNextLimit) delayNextLimit = timeLeft;
                }
            }
        }

        if(canvis)
            Funcions.write2File(getApplicationContext(),blockedApps);

        DateTime dateTime = new DateTime();
        int now = dateTime.getMillisOfDay();

        List<HorarisNit> horarisNits = Funcions.readFromFile(getApplicationContext(),Constants.FILE_HORARIS_NIT,false);
        HorarisNit avui = horarisNits.get(horarisNits.indexOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
        long delayNit;

        // delayNit és igual al temps fins que toca anar a dormir o fins a despertar demà
        if(avui.dormir > now) delayNit = avui.dormir - now;
        else{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_WEEK,1);
            HorarisNit dema = horarisNits.get(horarisNits.indexOf(calendar.get(Calendar.DAY_OF_WEEK)));

            delayNit = (TOTAL_MILLIS_IN_DAY - now) + dema.despertar;
        }

        Funcions.runLimitAppsWorker(getApplicationContext(), Math.min(delayNextLimit, delayNit));

        return Result.success();
    }
}
