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
import android.widget.Toast;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.BlockActivity;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.rest.TodoApi;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WindowChangeDetectingService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
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
                        TodoApi mTodoService = ((TodoApp)getApplicationContext()).getAPI();

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
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                            liveApp.appName = appInfo.loadLabel(getPackageManager()).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            liveApp.appName = componentName.getPackageName();
                        }
                        liveApp.time = time;
                        System.out.println("CurrentApp: "+liveApp.appName+ " |"+((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0));
                        if(!TodoApp.blackListLiveApp.contains(componentName.getPackageName())){
                            Call<String> call = ((TodoApp) getApplication()).getAPI().sendTutorLiveApp(TodoApp.getIDChild(), liveApp);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if (response.isSuccessful()) {
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                }
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
