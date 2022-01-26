package com.adictic.client.workers;

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

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class AppUsageWorker extends Worker {

    @Inject
    AdicticRepository repository;

    private static final int TOTAL_RETRIES = 5;
    private AdicticApi mTodoService;
    private SharedPreferences sharedPreferences;
    private int retryCountLastApp = 0;

    public AppUsageWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String TAG = "AppUsageWorker";

        mTodoService = repository.getApi();

        Log.d(TAG, "Starting Worker");
        sharedPreferences =repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;

        checkInstalledApps();

        repository.sendAppUsage();

        return Result.success();
    }

    private void checkInstalledApps() {
//        if(!sharedPreferences.contains("installedApps") || !sharedPreferences.getBoolean("installedApps",false)) {
            final List<AppInfo> listInstalledPkgs = getLaunchableApps();
            Call<String> call = mTodoService.postInstalledApps(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1), listInstalledPkgs);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
//        }
    }

    private List<AppInfo> getLaunchableApps() {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager mPm = getApplicationContext().getPackageManager();

//        List<String> launchers = Funcions.getLaunchers(getApplicationContext());

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> list = mPm.queryIntentActivities(main, 0);

        Map<String, AppInfo> hashMap = new HashMap<>();

        for (ResolveInfo ri : list) {
            // Si és launcher l'ignorem
//            if(launchers.contains(ri.activityInfo.packageName))
//                continue;

            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            if(!hashMap.containsKey(ai.packageName)){
                AppInfo appInfo = new AppInfo();
                appInfo.appName = mPm.getApplicationLabel(ai).toString();
                appInfo.pkgName = ai.packageName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category = ai.category;
                }
                hashMap.put(ai.packageName, appInfo);
            }
        }

        return new ArrayList<>(hashMap.values());
    }

//    private List<String> getLauncherApps(PackageManager mPm){
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
