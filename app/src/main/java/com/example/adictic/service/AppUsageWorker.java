package com.example.adictic.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUsageWorker extends Worker {
    Boolean ok1 = null;
    Boolean ok2 = null;
    int timeout = 0;

    SharedPreferences sharedPreferences;

    public AppUsageWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String TAG = "AppUsageWorker";

        Log.d(TAG, "Starting Worker");
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), sharedPreferences.getInt("dayOfYear",Calendar.getInstance().get(Calendar.DAY_OF_YEAR)), Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        TodoApi mTodoService = ((TodoApp) getApplicationContext()).getAPI();

        Funcions.canviarMesosAServidor(gul);

        Call<String> call = mTodoService.sendAppUsage(sharedPreferences.getLong("idUser",-1), gul);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                ok1 = response.isSuccessful();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                ok1 = false;
            }
        });

        Call<BlockedLimitedLists> call2 = mTodoService.getBlockedLimitedLists(sharedPreferences.getLong("idUser",-1));
        call2.enqueue(new Callback<BlockedLimitedLists>() {
            @Override
            public void onResponse(@NonNull Call<BlockedLimitedLists> call2, @NonNull Response<BlockedLimitedLists> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Funcions.updateDB_BlockedApps(getApplicationContext(),response.body());
                    if(!Funcions.fileEmpty(getApplicationContext(), Constants.FILE_BLOCKED_APPS))
                        Funcions.runLimitAppsWorker(getApplicationContext(), 0);
                    ok2 = true;
                } else {
                    if(!Funcions.fileEmpty(getApplicationContext(), Constants.FILE_BLOCKED_APPS))
                        Funcions.runLimitAppsWorker(getApplicationContext(), 0);
                    ok2 = false;
                }
            }

            @Override
            public void onFailure(@NonNull Call<BlockedLimitedLists> call2, @NonNull Throwable t) {
                if(!Funcions.fileEmpty(getApplicationContext(), Constants.FILE_BLOCKED_APPS))
                    Funcions.runLimitAppsWorker(getApplicationContext(), 0);
                ok2 = false;
            }
        });

        // Indicate whether the task finished successfully with the Result
        long now = System.currentTimeMillis();
        while (ok1 == null || ok2 == null) {
            if (System.currentTimeMillis() - now > 30 * 1000) {
                ok1 = ok2 = false;
            }
        }
        if (ok1 && ok2) {
            Log.d(TAG, "Result OK");
            sharedPreferences.edit().putInt("dayOfYear",Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
            timeout = 0;
            return Result.success();
        } else if (timeout < 5) {
            Log.d(TAG, "Result RETRY");
            timeout++;
            return Result.retry();
        } else return Result.failure();
    }
}
