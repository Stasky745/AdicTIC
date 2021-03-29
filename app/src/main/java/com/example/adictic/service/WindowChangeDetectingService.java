package com.example.adictic.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.adictic.TodoApp;
import com.example.adictic.activity.BlockActivity;
import com.example.adictic.activity.OficinesActivity;
import com.example.adictic.entity.AppChange;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.rest.TodoApi;

import org.apache.commons.collections4.CollectionUtils;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WindowChangeDetectingService extends AccessibilityService {

    TodoApi mTodoService;
    PackageManager mPm;
    List<AppInfo> lastListApps;
    Calendar dayUpdatedInstalledApps;
    Calendar lastTryUpdate;
    List<AppChange> uninstalledApps;
    List<AppChange> installedApps;

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        uninstalledApps = new ArrayList<>();
        installedApps = new ArrayList<>();
        dayUpdatedInstalledApps = Calendar.getInstance();
        dayUpdatedInstalledApps.add(Calendar.DAY_OF_YEAR,-1);
        lastTryUpdate = Calendar.getInstance();
        lastTryUpdate.add(Calendar.DAY_OF_YEAR,-1);

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        mPm = getPackageManager();
        lastListApps = new ArrayList<>();
        if(TodoApp.getIDChild() != -1) checkInstalledApps();

        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    private List<AppInfo> getLaunchableApps(){
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        List<AppInfo> res = new ArrayList<>();

        List<ResolveInfo> list = mPm.queryIntentActivities(main,0);

        //List<String> launcherApps = getLauncherApps();

        List<String> duplicatesList = new ArrayList<>();

        for(ResolveInfo ri : list){
            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            // if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && launcherApps.contains(ai.packageName) && !duplicatesList.contains(ai.packageName)) {
            if(!duplicatesList.contains(ai.packageName)) {
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

    private void setChangedAppLists(List<AppInfo> listNewPkgs, Calendar today){
        Collection<AppInfo> differentApps = CollectionUtils.disjunction(listNewPkgs,lastListApps);

        for(AppInfo ai : differentApps){
            AppChange ac = new AppChange();
            ac.pkgName = ai.pkgName;
            ac.day = today.get(Calendar.YEAR)+"/"+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DAY_OF_MONTH);

            if(listNewPkgs.contains(ai)) installedApps.add(ac);
            else uninstalledApps.add(ac);
        }
    }

    private void checkInstalledApps(){
        final List<AppInfo> listInstalledPkgs = getLaunchableApps();

        Calendar today = Calendar.getInstance();
        if(!CollectionUtils.isEqualCollection(listInstalledPkgs,lastListApps) || (today.get(Calendar.DAY_OF_YEAR) != dayUpdatedInstalledApps.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.DAY_OF_YEAR) != lastTryUpdate.get(Calendar.DAY_OF_YEAR))) {
            setChangedAppLists(listInstalledPkgs, today);

            lastTryUpdate = Calendar.getInstance();

            Call<String> call = mTodoService.postInstalledApps(TodoApp.getIDChild(),listInstalledPkgs);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()){
                        lastListApps = listInstalledPkgs;
                        dayUpdatedInstalledApps = Calendar.getInstance();

                        installedApps.clear();
                        uninstalledApps.clear();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) { }
            });
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

            if(TodoApp.getIDChild() != -1) checkInstalledApps();

            //checkInstalledApps(); /** Borrar després, aquí per fer proves **/

            if(TodoApp.getBlockedDevice() || !TodoApp.getBlockEvents().isEmpty()){
                KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
                if(!myKM.isDeviceLocked()){
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
                if (isActivity){
                    Log.i("CurrentActivity", componentName.flattenToShortString());
                    Log.i("CurrentPackage", componentName.getPackageName());

                    String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                    if (TodoApp.getStartFreeUse()!=1 && TodoApp.getBlockedApps().contains(componentName.getPackageName())) {

                        Call<String> call = mTodoService.callBlockedApp(TodoApp.getIDChild(),componentName.getPackageName());
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.isSuccessful()) { }
                            }
                            @Override
                            public void onFailure(Call<String> call, Throwable t) { }
                        });

                        Intent lockIntent = new Intent(this, BlockActivity.class);
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(lockIntent);
                    }
                    Log.i("LiveApp", TodoApp.getLiveApp()+
                            " "+TodoApp.getIDChild());
                    if(TodoApp.getLiveApp() && TodoApp.getIDChild() != -1){
                        LiveApp liveApp = new LiveApp();
                        liveApp.pkgName = componentName.getPackageName();
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                            liveApp.appName = appInfo.loadLabel(getPackageManager()).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            liveApp.appName = componentName.getPackageName();
                        }
                        liveApp.time = time;
                        if(!TodoApp.blackListLiveApp.contains(componentName.getPackageName())){
                            Call<String> call = ((TodoApp) getApplication()).getAPI().sendTutorLiveApp(TodoApp.getIDChild(), liveApp);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if (response.isSuccessful()) { }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) { }
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
    public void onInterrupt() {}
}
