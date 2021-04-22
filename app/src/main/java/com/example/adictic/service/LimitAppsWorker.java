package com.example.adictic.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.Horaris;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.roomdb.BlockedApp;
import com.example.adictic.roomdb.FreeUseApp;
import com.example.adictic.roomdb.HorarisNit;
import com.example.adictic.roomdb.RoomRepo;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LimitAppsWorker extends Worker {
    int TOTAL_MILLIS_IN_DAY = 86400000;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), 0, -1);

        RoomRepo roomRepo = new RoomRepo(getApplicationContext());

        List<BlockedApp> blockedApps = roomRepo.getAllNotBlockedApps();
        List<FreeUseApp> freeUseApps = roomRepo.getAllFreeUseApps();

        //checkHoraris();

        List<AppUsage> listCurrentUsage = (List<AppUsage>) gul.get(0).usage;

        long delayNextLimit = Long.MAX_VALUE;

        for(BlockedApp app : blockedApps){
            AppUsage appUsage = listCurrentUsage.get(listCurrentUsage.indexOf(app.pkgName));

            // tenim en compte el temps que l'app ha estat utilitzada durant FreeUse
            long freeUseUsage = 0;
            if(freeUseApps.contains(app.pkgName))
                freeUseUsage = freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageEnd - freeUseApps.get(freeUseApps.indexOf(app.pkgName)).millisUsageStart;
            if(appUsage.totalTime >= app.timeLimit + freeUseUsage){
                app.blockedNow = true;
                roomRepo.updateBlockApp(app);
            }
            else{
                long timeLeft = app.timeLimit - appUsage.totalTime;
                if(timeLeft < delayNextLimit) delayNextLimit = timeLeft;
            }
        }

        DateTime dateTime = new DateTime();
        int now = dateTime.getMillisOfDay();

        List<HorarisNit> horarisNits = roomRepo.getAllHorarisNit();
        HorarisNit avui = horarisNits.get(horarisNits.indexOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
        long delayNit;

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

    private void checkHoraris() {

        TodoApi mTodoService = ((TodoApp) getApplicationContext()).getAPI();

        Call<Horaris> call = mTodoService.getHoraris(sharedPreferences.getLong("idUser",-1));

        call.enqueue(new Callback<Horaris>() {
            @Override
            public void onResponse(@NonNull Call<Horaris> call, @NonNull Response<Horaris> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setHoraris(response.body().horarisNits);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Horaris> call, @NonNull Throwable t) {

            }
        });
    }

    private void setHoraris(List<HorarisNit> list) {
        //per cada dia (Sunday = 1) -> (Saturday = 7)
        for(HorarisNit horarisNit : list){
            RoomRepo roomRepo = new RoomRepo(getApplicationContext());
            roomRepo.insertHorarisNit(horarisNit);
        }
    }
}
