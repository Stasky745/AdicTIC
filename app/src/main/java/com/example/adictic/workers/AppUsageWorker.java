package com.example.adictic.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUsageWorker extends Worker {
    Boolean ok;

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

        // Inicialitzem el GeoLocWorker si no existeix
        Funcions.runGeoLocWorker(getApplicationContext());

        checkInstalledApps();

        List<GeneralUsage> gul = Funcions.getGeneralUsages(getApplicationContext(), sharedPreferences.getInt("dayOfYear",Calendar.getInstance().get(Calendar.DAY_OF_YEAR)), Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

        TodoApi mTodoService = ((TodoApp) getApplicationContext()).getAPI();

        //Funcions.canviarMesosAServidor(gul);

        ok = true;

        Call<String> call = mTodoService.sendAppUsage(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), gul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                sharedPreferences.edit().putInt(Constants.SHARED_PREFS_DAYOFYEAR,Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                ok = false;
            }
        });

        if(ok)
            return Result.success();
        else
            return Result.retry();
    }

    private void checkInstalledApps() {
//        if(!sharedPreferences.contains("installedApps") || !sharedPreferences.getBoolean("installedApps",false)) {
            final List<AppInfo> listInstalledPkgs = getLaunchableApps();

            TodoApi mTodoService = ((TodoApp) getApplicationContext()).getAPI();
            Call<String> call = mTodoService.postInstalledApps(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1), listInstalledPkgs);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
//                        sharedPreferences.edit().putBoolean("installedApps",true).apply();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                }
            });
//        }
    }

    private List<AppInfo> getLaunchableApps() {
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager mPm = getApplicationContext().getPackageManager();

        List<AppInfo> res = new ArrayList<>();

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> list = mPm.queryIntentActivities(main, 0);

        //List<String> launcherApps = getLauncherApps();

        List<String> duplicatesList = new ArrayList<>();

        for (ResolveInfo ri : list) {
            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            // if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && launcherApps.contains(ai.packageName) && !duplicatesList.contains(ai.packageName)) {
            if (!duplicatesList.contains(ai.packageName)) {
                duplicatesList.add(ai.packageName);
                AppInfo appInfo = new AppInfo();
                appInfo.appName = mPm.getApplicationLabel(ai).toString();
                appInfo.pkgName = ai.packageName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category = ai.category;
                }
                res.add(appInfo);
            }
        }

        return res;
    }

//    private List<String> getLauncherApps(){
//        List<ApplicationInfo> list = mPm.getInstalledApplications(0);
//
//        List<String> res = new ArrayList<>();
//
//        for(ApplicationInfo ai : list){
//            if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) res.add(ai.packageName);
//        }
//
//        return res;
//    }
}
