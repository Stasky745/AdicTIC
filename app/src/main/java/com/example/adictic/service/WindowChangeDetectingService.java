package com.example.adictic.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.adictic.TodoApp;
import com.example.adictic.activity.BlockActivity;
import com.example.adictic.entity.InstalledApp;
import com.example.adictic.rest.TodoApi;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.collections4.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WindowChangeDetectingService extends AccessibilityService {

    TodoApi mTodoService;
    PackageManager mPm;
    List<ApplicationInfo> lastListApps;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        mPm = getPackageManager();
        lastListApps = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
        System.out.println("LAST LIST:"+lastListApps);


        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    private void checkInstalledApps(){
        List<ApplicationInfo> listInstalledPkgs = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
        if(!CollectionUtils.isEqualCollection(listInstalledPkgs,lastListApps)) {
            List<InstalledApp> listApps = new ArrayList<>();

            for (ApplicationInfo ai : listInstalledPkgs) {
                InstalledApp iApp = new InstalledApp();

                iApp.pkgName = ai.packageName;
                iApp.appName = mPm.getApplicationLabel(ai).toString();

                listApps.add(iApp);
            }
        }

        //Call<String> call = mTodoService.postInstalledApps()
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            checkInstalledApps();

            if(TodoApp.getBlockedDevice()){
                DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDPM.lockNow();
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

                    String time = new SimpleDateFormat("HH:mm").format(new Date());

                    if (TodoApp.getStartFreeUse()!=0 && TodoApp.getBlockedApps().contains(componentName.getPackageName())) {

                        Call<String> call = mTodoService.callBlockedApp(TodoApp.getID(),componentName.getPackageName());
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

                    if(TodoApp.getLiveApp() && TodoApp.getTutorToken() != null){
                        FirebaseMessaging fm = FirebaseMessaging.getInstance();
                        fm.send(new RemoteMessage.Builder(TodoApp.getTutorToken() + "@fcm.googleapis.com")
                                .addData("currentAppUpdate", componentName.getPackageName())
                                .addData("time", time)
                                .build());
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
