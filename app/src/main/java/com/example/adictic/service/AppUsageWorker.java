package com.example.adictic.service;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.Global;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUsageWorker extends Worker {
    final Long idChild = getInputData().getLong("id",-1);
    Boolean ok;

    public AppUsageWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);;
        PackageManager mPm = getApplicationContext().getPackageManager();

        Funcions.runLimitAppsWorker(getApplicationContext());

        TodoApi mTodoService = ((TodoApp)getApplicationContext()).getAPI();
        List<GeneralUsage> gul = new ArrayList<>();
        List<UsageStats> stats;
        for (int i = Global.dayOfYear; i < Calendar.getInstance().get(Calendar.DAY_OF_YEAR); i++) {
            Calendar finalTime = Calendar.getInstance();
            finalTime.set(Calendar.DAY_OF_YEAR, i);
            finalTime.set(Calendar.HOUR_OF_DAY, 23);
            finalTime.set(Calendar.MINUTE, 59);
            finalTime.set(Calendar.SECOND, 59);
            System.out.println(finalTime.getTime());

            Calendar initialTime = Calendar.getInstance();
            initialTime.set(Calendar.DAY_OF_YEAR, i);
            initialTime.set(Calendar.HOUR_OF_DAY, 0);
            initialTime.set(Calendar.MINUTE, 0);
            initialTime.set(Calendar.SECOND, 0);
            System.out.println(initialTime.getTime());
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                    initialTime.getTimeInMillis(), finalTime.getTimeInMillis());

            List<AppUsage> appUsages = new ArrayList<>();
            final int statCount = stats.size();
            for (int j = 0; j < statCount; j++) {
                final android.app.usage.UsageStats pkgStats = stats.get(j);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (pkgStats.getLastTimeUsed() >= initialTime.getTimeInMillis() && pkgStats.getLastTimeUsed() <= finalTime.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    AppUsage appUsage = new AppUsage();
                    appUsage.appName = pkgStats.getPackageName();
                    appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                    appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                    appUsages.add(appUsage);
                }
            }
            GeneralUsage gu = new GeneralUsage();
            gu.day = finalTime.get(Calendar.DAY_OF_MONTH);
            gu.month = finalTime.get(Calendar.MONTH) + 1;
            gu.year = finalTime.get(Calendar.YEAR);
            gu.usage = appUsages;
            gul.add(gu);
        }

        Call<String> call = mTodoService.sendAppUsage(idChild,gul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    ok = true;
                }
                else ok = false;
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ok = false;
            }
        });

        // Indicate whether the task finished successfully with the Result
        if(ok){
            Global.dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            return Result.success();
        }
        else return Result.retry();
    }
}
