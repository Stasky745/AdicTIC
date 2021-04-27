package com.example.adictic.workers.block_apps;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.BlockedApp;
import com.example.adictic.entity.HorarisNit;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;
import java.util.Calendar;
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

        List<BlockedApp> blockedApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_BLOCKED_APPS,false);

        // Afegim les apps que estan bloquejades permanentment i cap altra
        List<String> permanentBlockedApps;
        if(blockedApps == null) {
            permanentBlockedApps = new ArrayList<>();
            Funcions.write2File(getApplicationContext(), permanentBlockedApps);
            return Result.success();
        }
        else
            permanentBlockedApps = blockedApps.stream().filter(blockedApp -> blockedApp.timeLimit < 0)
                    .map(blockedApp -> blockedApp.pkgName)
                    .collect(Collectors.toList());
        Funcions.write2File(getApplicationContext(),permanentBlockedApps);

        long delay = 0;

        // Si el dispositiu està bloquejat durant la nit, afegim al delay el temps fins que s'hagi de despertar
        List<HorarisNit> horarisNits = Funcions.readFromFile(getApplicationContext(),Constants.FILE_HORARIS_NIT,false);
        if(horarisNits != null && !horarisNits.isEmpty())
            delay = horarisNits.stream().filter(obj -> obj.idDia.equals(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))).findAny().get().despertar;

        // Afegim al delay el temps mínim fins que es pugui bloquejar una app
        long minTimeAllowed = Constants.TOTAL_MILLIS_IN_DAY;
        for(BlockedApp blockedApp : blockedApps){
            if(blockedApp.timeLimit < minTimeAllowed)
                minTimeAllowed = blockedApp.timeLimit;
        }
        delay += minTimeAllowed;

        Funcions.runBlockAppsWorker(getApplicationContext(),delay);

        return Result.success();
    }
}
