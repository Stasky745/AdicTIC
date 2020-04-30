package com.example.adictic.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.LimitedApps;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUsageWorker extends Worker {
    Boolean ok1 = null;
    Boolean ok2 = null;
    int timeout = 0;

    public AppUsageWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        String TAG = "AppUsageWorker";

        Log.d(TAG, "Starting Worker");

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), TodoApp.getDayOfYear(), Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        if(!TodoApp.getLimitApps().isEmpty()) Funcions.runLimitAppsWorker(getApplicationContext(),0);
        if(TodoApp.getStartFreeUse() != 0) TodoApp.setStartFreeUse(Calendar.getInstance().getTimeInMillis());

        TodoApi mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        for(GeneralUsage gu : gul){

        }

        Call<String> call = mTodoService.sendAppUsage(TodoApp.getID(),gul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    ok1 = true;
                }
                else ok1 = false;
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ok1 = false;
            }
        });

        Call<BlockedLimitedLists> call2 = mTodoService.getBlockedLimitedLists(TodoApp.getID());
        call2.enqueue(new Callback<BlockedLimitedLists>() {
            @Override
            public void onResponse(Call<BlockedLimitedLists> call2, Response<BlockedLimitedLists> response) {
                if (response.isSuccessful()) {
                    TodoApp.setBlockedApps(response.body().blockedApps);
                    List<LimitedApps> limitList = response.body().limitApps;
                    Map<String,Long> finalMap = new HashMap<>();
                    for(LimitedApps limit : limitList){
                        finalMap.put(limit.name,limit.time);
                    }
                    TodoApp.setLimitApps(finalMap);
                    ok2 = true;
                }
                else ok2 = false;
            }
            @Override
            public void onFailure(Call<BlockedLimitedLists> call2, Throwable t) {
                ok2 = false;
            }
        });

        // Indicate whether the task finished successfully with the Result
        while(ok1 == null && ok2 == null){}
        if(ok1 && ok2){
            Log.d(TAG, "Result OK");
            TodoApp.setDayOfYear(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
            timeout = 0;
            return Result.success();
        }
        else if(timeout<5){
            Log.d(TAG, "Result RETRY");
            timeout++;
            return Result.retry();
        }
        else return Result.failure();
    }
}
