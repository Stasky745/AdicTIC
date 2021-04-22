package com.example.adictic.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.example.adictic.entity.AppChange;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.roomdb.BlockedApp;
import com.example.adictic.roomdb.RoomRepo;
import com.example.adictic.ui.BlockScreenActivity;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import org.apache.commons.collections4.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WindowChangeDetectingService extends AccessibilityService {

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
    private final List<String> blackListLiveApp = Collections.singletonList("com.google.android.apps.nexuslauncher");
    TodoApi mTodoService;
    SharedPreferences sharedPreferences;
    PackageManager mPm;
    List<AppInfo> lastListApps;
    Calendar dayUpdatedInstalledApps;
    Calendar lastTryUpdate;
    List<AppChange> uninstalledApps;
    List<AppChange> installedApps;

    String lastActivity;
    String lastPackage;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mTodoService = ((TodoApp) getApplicationContext()).getAPI();

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        uninstalledApps = new ArrayList<>();
        installedApps = new ArrayList<>();
        dayUpdatedInstalledApps = Calendar.getInstance();
        dayUpdatedInstalledApps.add(Calendar.DAY_OF_YEAR, -1);
        lastTryUpdate = Calendar.getInstance();
        lastTryUpdate.add(Calendar.DAY_OF_YEAR, -1);

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        mPm = getPackageManager();
        lastListApps = new ArrayList<>();
        if (!sharedPreferences.getBoolean("isTutor",false)) checkInstalledApps();

        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    private List<AppInfo> getLaunchableApps() {
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

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

    private void setChangedAppLists(List<AppInfo> listNewPkgs, Calendar today) {
        Collection<AppInfo> differentApps = CollectionUtils.disjunction(listNewPkgs, lastListApps);

        for (AppInfo ai : differentApps) {
            AppChange ac = new AppChange();
            ac.pkgName = ai.pkgName;
            ac.day = today.get(Calendar.YEAR) + "/" + (today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH);

            if (listNewPkgs.contains(ai)) installedApps.add(ac);
            else uninstalledApps.add(ac);
        }
    }

    private void checkInstalledApps() {
        final List<AppInfo> listInstalledPkgs = getLaunchableApps();

        Calendar today = Calendar.getInstance();
        if (!CollectionUtils.isEqualCollection(listInstalledPkgs, lastListApps) || (today.get(Calendar.DAY_OF_YEAR) != dayUpdatedInstalledApps.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.DAY_OF_YEAR) != lastTryUpdate.get(Calendar.DAY_OF_YEAR))) {
            setChangedAppLists(listInstalledPkgs, today);

            lastTryUpdate = Calendar.getInstance();

            Call<String> call = mTodoService.postInstalledApps(sharedPreferences.getLong("idUser",-1), listInstalledPkgs);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        lastListApps = listInstalledPkgs;
                        dayUpdatedInstalledApps = Calendar.getInstance();

                        installedApps.clear();
                        uninstalledApps.clear();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                }
            });
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean freeUseEnabled = sharedPreferences.getBoolean("freeUse",false);
        if (!freeUseEnabled && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            RoomRepo roomRepo = new RoomRepo(getApplicationContext());

            Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

            if (!sharedPreferences.getBoolean("isTutor",false)) checkInstalledApps();

            // Enviem l'última app oberta a la mare
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
            if(myKM.isDeviceLocked()){
                //String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                LiveApp liveApp = new LiveApp();
                liveApp.pkgName = lastPackage;
                liveApp.appName = lastActivity;
                liveApp.time = Calendar.getInstance().getTimeInMillis();

                if (!blackListLiveApp.contains(lastPackage)) {
                    Call<String> call = ((TodoApp) getApplication()).getAPI().postLastAppUsed(sharedPreferences.getLong("idUser",-1), liveApp);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
                    });
                }
            }

            // Bloquegem dispositiu si està bloquejat o té un event en marxa
            boolean estaBloquejat = sharedPreferences.getBoolean("blockedDevice",false);
            if (estaBloquejat || !roomRepo.getAllActiveEvents().isEmpty()) {
                if (!myKM.isDeviceLocked()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    assert mDPM != null;
                    mDPM.lockNow();
                }
            }
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    ApplicationInfo appInfo = null;
                    if (!blackListLiveApp.contains(componentName.getPackageName())) {
                        try {
                            appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                            lastActivity = appInfo.loadLabel(getPackageManager()).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            lastActivity = componentName.getPackageName();
                        }
                        lastPackage = componentName.getPackageName();
                        Log.i("CurrentActivity", componentName.flattenToShortString());
                        Log.i("CurrentPackage", componentName.getPackageName());
                    }

                    //String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                    boolean isBlocked = false;

                    if(roomRepo.getAllBlockedApps().contains(componentName.getPackageName())){
                        BlockedApp blockedApp = roomRepo.findBlockedAppByPkg(componentName.getPackageName());
                        isBlocked = blockedApp.blockedNow;
                    }

                    if (isBlocked) {
                        Call<String> call = mTodoService.callBlockedApp(sharedPreferences.getLong("idUser",-1), componentName.getPackageName());
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
                        });

                        Intent lockIntent = new Intent(this, BlockScreenActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(lockIntent);
                    }

                    Log.i("LiveApp", sharedPreferences.getBoolean("liveApp",false) +
                            " " + sharedPreferences.getLong("idUser",-1));

                    if (sharedPreferences.getBoolean("liveApp",false)) {
                        LiveApp liveApp = new LiveApp();
                        liveApp.pkgName = componentName.getPackageName();

                        if(appInfo == null)
                            liveApp.appName = componentName.getPackageName();
                        else
                            liveApp.appName = appInfo.loadLabel(getPackageManager()).toString();

                        liveApp.time = Calendar.getInstance().getTimeInMillis();

//                        ApplicationInfo appInfo;
//                        try {
//                            appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
//                            liveApp.appName = appInfo.loadLabel(getPackageManager()).toString();
//                        } catch (PackageManager.NameNotFoundException e) {
//                            liveApp.appName = componentName.getPackageName();
//                        }

                        //mirar si component.getpackagename() funciona si ho fem amb lastPackage
                        if (!blackListLiveApp.contains(componentName.getPackageName())) {
                            Call<String> call = ((TodoApp) getApplication()).getAPI().sendTutorLiveApp(sharedPreferences.getLong("idUser",-1), liveApp);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
                            });
                        }
                    }
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }
}
