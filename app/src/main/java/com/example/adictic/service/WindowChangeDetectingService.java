package com.example.adictic.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.ui.BlockScreenActivity;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WindowChangeDetectingService extends AccessibilityService {

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
    private final List<String> blackListLiveApp = Collections.singletonList("com.google.android.apps.nexuslauncher");
    TodoApi mTodoService;
    SharedPreferences sharedPreferences;
    PackageManager mPm;

    List<String> blockedApps;

    String lastActivity;
    String lastPackage;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        assert sharedPreferences != null;
        if(sharedPreferences.getBoolean("isTutor",false)) {
            disableSelf();
        }
        else {
            fetchDades();

            mTodoService = ((TodoApp) getApplicationContext()).getAPI();
            mPm = getPackageManager();

            blockedApps = new ArrayList<>();

            lastActivity = "";
            lastPackage = "";

            //Configure these here for compatibility with API 13 and below.
            AccessibilityServiceInfo config = getServiceInfo();

            config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            setServiceInfo(config);
        }
    }

    private void fetchDades() {
        mTodoService = ((TodoApp) getApplicationContext()).getAPI();
        if(sharedPreferences.contains("userId")) {
            Call<BlockedLimitedLists> call = mTodoService.getBlockedLimitedLists(sharedPreferences.getLong("userId", -1));
            call.enqueue(new Callback<BlockedLimitedLists>() {
                @Override
                public void onResponse(@NonNull Call<BlockedLimitedLists> call, @NonNull Response<BlockedLimitedLists> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Funcions.updateDB_BlockedApps(getApplicationContext(), response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BlockedLimitedLists> call, @NonNull Throwable t) {

                }
            });

            Funcions.checkHoraris(getApplicationContext());
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

            // Si és FreeUse, tornem sense fer res
            if(sharedPreferences.getBoolean("freeUse",false)){
                Log.d(TAG, "Not FreeUse, return.");
                return;
            }

            if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_CHANGE_BLOCKED_APPS,false))
                blockedApps = Funcions.readFromFile(getApplicationContext(),Constants.FILE_CURRENT_BLOCKED_APPS,true);

            // Enviem l'última app oberta a la mare si el dispositiu s'ha bloquejat
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
            if(myKM.isDeviceLocked()){
                enviarLastApp();
            }

            // Bloquegem dispositiu si està bloquejat o té un event en marxa
            boolean estaBloquejat = sharedPreferences.getBoolean("blockedDevice",false) || sharedPreferences.getBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT,false);

            int currentActiveEvents = sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, 0);

            if (estaBloquejat || currentActiveEvents > 0) {
                if (!myKM.isDeviceLocked()) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    assert mDPM != null;
                    mDPM.lockNow();
                }
            }
            //
            else if (event.getPackageName() != null && event.getClassName() != null && !event.getPackageName().equals(lastPackage)) {
                Log.d(TAG,"L'event no és null - Entra a 'else if'");

                // Agafem info de l'Event
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;

                if (isActivity) {
                    Log.d(TAG,"L'event és una activitat");
                    ApplicationInfo appInfo;

                    if (!blackListLiveApp.contains(componentName.getPackageName())) {
                        Log.d(TAG,"L'event no està a 'blackListLiveApp'");

                        // Actualitzem les llistes d'Events i BlockedApp
                        actualitzarLlistes();

                        try {
                            appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                            lastActivity = appInfo.loadLabel(getPackageManager()).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            lastActivity = componentName.getPackageName();
                        }
                        lastPackage = componentName.getPackageName();
                        Log.d(TAG,"Llista Apps Bloquejades : " + blockedApps);
                        Log.d(TAG,"CurrentActivity : " + componentName.flattenToShortString());
                        Log.d(TAG,"CurrentPackage : " + lastPackage);

                        //Mirem si l'app està bloquejada
                        boolean isBlocked = isBlocked();

                        if (isBlocked) {
                            ensenyarBlockScreenActivity();
                        }

                        Log.i("LiveApp", sharedPreferences.getBoolean("liveApp",false) +
                                "   idChild: " + sharedPreferences.getLong("idUser",-1));

                        if (sharedPreferences.getBoolean("liveApp",false)) {
                            enviarLiveApp();
                        }
                    }
                }
            }
        }
    }

    private void enviarLiveApp() {
        LiveApp liveApp = new LiveApp();
        liveApp.pkgName = lastPackage;
        liveApp.appName = lastActivity;
        liveApp.time = Calendar.getInstance().getTimeInMillis();

        Call<String> call = ((TodoApp) getApplication()).getAPI().sendTutorLiveApp(sharedPreferences.getLong("idUser",-1), liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
        });
    }

    private void ensenyarBlockScreenActivity() {
        Call<String> call = mTodoService.callBlockedApp(sharedPreferences.getLong("idUser",-1), lastPackage);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
        });

        Log.d(TAG,"Creant Intent cap a BlockScreenActivity");
        Intent lockIntent = new Intent(WindowChangeDetectingService.this, BlockScreenActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        lockIntent.putExtra("pkgName",lastPackage);
        startActivity(lockIntent);
    }

    private boolean isBlocked() {
        return blockedApps.contains(lastPackage);
    }

    private void enviarLastApp() {
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

    private void actualitzarLlistes() {
        // creem llistes buides si no estan inicialitzades
        boolean primerCop = false;
//        if(eventBlocks == null) {
//            eventBlocks = new ArrayList<>();
//            primerCop = true;
//        }

        if(blockedApps == null) {
            blockedApps = new ArrayList<>();
            primerCop = true;
        }

//        // Actualitzem la llista de EventBlock
//        if(primerCop || sharedPreferences.getBoolean(Constants.SHARED_PREFS_CHANGE_EVENT_BLOCK,false)) {
//            eventBlocks = Funcions.readFromFile(getApplicationContext(),Constants.FILE_EVENT_BLOCK,true);
//            if(eventBlocks == null)
//                eventBlocks = new ArrayList<>();
//        }

        // Actualitzem la llista de BlockedApps amb només les apps bloquejades ara mateix
        if(primerCop || sharedPreferences.getBoolean(Constants.SHARED_PREFS_CHANGE_BLOCKED_APPS,false)){
            List<BlockedApp> llista = Funcions.readFromFile(getApplicationContext(),Constants.FILE_CURRENT_BLOCKED_APPS,true);
            if(llista == null)
                blockedApps = new ArrayList<>();
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
