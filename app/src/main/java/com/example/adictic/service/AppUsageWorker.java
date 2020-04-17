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

        TodoApi mTodoService = ((TodoApp)getApplicationContext()).getAPI();
        List<GeneralUsage> gul = new ArrayList<>();
        List<UsageStats> stats;
        for (int i = Global.dayOfYear; i < Calendar.DAY_OF_YEAR; i++) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.add(Calendar.DAY_OF_YEAR, i);
            System.out.println(cal.getTime());

            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.DAY_OF_YEAR, i);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            System.out.println(cal2.getTime());
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                    cal2.getTimeInMillis(), cal.getTimeInMillis());

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
                if (pkgStats.getLastTimeUsed() >= cal2.getTimeInMillis() && pkgStats.getLastTimeUsed() <= cal.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    AppUsage appUsage = new AppUsage();
                    appUsage.appName = pkgStats.getPackageName();
                    appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                    appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                    appUsages.add(appUsage);
                }
            }
            GeneralUsage gu = new GeneralUsage();
            gu.day = cal.get(Calendar.DAY_OF_MONTH);
            gu.month = cal.get(Calendar.MONTH) + 1;
            gu.year = cal.get(Calendar.YEAR);
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
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                ok = false;
            }
        });
        // Indicate whether the task finished successfully with the Result
        if(ok){
            Global.dayOfYear = Calendar.DAY_OF_YEAR;
            return Result.success();
        }
        else return Result.failure();
    }
}
