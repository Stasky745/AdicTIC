package com.example.adictic.service;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;

import org.joda.time.DateTime;

import java.util.ArrayList;
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

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), 0, -1);

        List<HorarisNit> horarisNits = Funcions.readFromFile(getApplicationContext(),Constants.FILE_HORARIS_NIT,false);

        DateTime dateTime = new DateTime();
        int now = dateTime.getMillisOfDay();

        HorarisNit avui = null;
        if(horarisNits == null) horarisNits = new ArrayList<>();
        else if(!horarisNits.isEmpty()) avui = horarisNits.stream().filter(obj -> obj.idDia.equals(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))).findAny().get();

        int millisAra = DateTime.now().getMillisOfDay();
        if(millisAra < avui.despertar){
            sharedPreferences.edit().putBoolean("blockedDeviceNit",true).apply();
            long delay = (avui.despertar - millisAra) + 1000*30; // delay fins hora de despertar + 30 segons per si de cas
            Funcions.runLimitAppsWorker(getApplicationContext(), delay);
            return Result.success();
        }
        else if(millisAra > avui.dormir){
            sharedPreferences.edit().putBoolean("blockedDeviceNit",true).apply();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_WEEK,1);
            HorarisNit dema = horarisNits.stream().filter(obj -> obj.idDia.equals(calendar.get(Calendar.DAY_OF_WEEK))).findAny().get();

            long delay = dema.despertar + (TOTAL_MILLIS_IN_DAY - millisAra) + 1000*30; // delay fins hora de despertar + 30 segons per si de cas
            Funcions.runLimitAppsWorker(getApplicationContext(), delay);
            return Result.success();
        }
        else
            sharedPreferences.edit().putBoolean("blockedDeviceNit",false).apply();

        // Agafem una llista amb les apps que falten per bloquejar
        List<BlockedApp> blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_BLOCKED_APPS,false);
        List<FreeUseApp> freeUseApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_FREE_USE_APPS,false);
        List<AppUsage> listCurrentUsage = (List<AppUsage>) gul.get(0).usage;

        long delayNextLimit = Long.MAX_VALUE;

        boolean canvis = false;

        for(BlockedApp app : blockedApps){
            if(!app.blockedNow && listCurrentUsage.stream().anyMatch(obj -> obj.app.pkgName.equals(app.pkgName))) {
                AppUsage appUsage = listCurrentUsage.stream().filter(obj -> obj.app.pkgName.equals(app.pkgName)).findFirst().get();

                // tenim en compte el temps que l'app ha estat utilitzada durant FreeUse
                long freeUseUsage = 0;
                if (freeUseApps.stream().anyMatch(obj -> obj.pkgName.equals(app.pkgName))) {
                    FreeUseApp freeUseApp = freeUseApps.stream().filter(obj -> obj.pkgName.equals(app.pkgName)).findFirst().get();
                    freeUseUsage = freeUseApp.millisUsageEnd - freeUseApp.millisUsageStart;
                }
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
                AppUsage appUsage = listCurrentUsage.stream().filter(obj -> obj.app.pkgName.equals(app.pkgName)).findAny().get();

                // tenim en compte el temps que l'app ha estat utilitzada durant FreeUse
                long freeUseUsage = 0;
                if (freeUseApps.stream().anyMatch(obj -> obj.pkgName.equals(app.pkgName))) {
                    FreeUseApp freeUseApp = freeUseApps.stream().filter(obj -> obj.pkgName.equals(app.pkgName)).findFirst().get();
                    freeUseUsage = freeUseApp.millisUsageEnd - freeUseApp.millisUsageStart;
                }
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

        long delayNit;

        // delayNit és igual al temps fins que toca anar a dormir o fins a despertar demà
        if(avui != null && avui.dormir > now) delayNit = avui.dormir - now;
        else if (!horarisNits.isEmpty()){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_WEEK,1);
            HorarisNit dema = horarisNits.stream().filter(obj -> obj.idDia.equals(calendar.get(Calendar.DAY_OF_WEEK))).findAny().get();

            delayNit = (TOTAL_MILLIS_IN_DAY - now) + dema.despertar;
        }
        else
            delayNit = TOTAL_MILLIS_IN_DAY - now;

        Funcions.runLimitAppsWorker(getApplicationContext(), Math.min(delayNextLimit, delayNit));

        return Result.success();
    }
}
