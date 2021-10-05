package com.adictic.client.service;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.BlockDeviceActivity;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.client.workers.BlockDeviceWorker;
import com.adictic.common.entity.LiveApp;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.client.entity.BlockedApp;
import com.adictic.client.workers.BlockSingleAppWorker;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class AccessibilityScreenService extends AccessibilityService {

    public static AccessibilityScreenService instance;

    private static final String TAG = AccessibilityScreenService.class.getSimpleName();
    private final List<String> blackListLiveApp = new ArrayList<>(Arrays.asList(
            "com.google.android.apps.nexuslauncher",
            "com.android.systemui",
            "com.miui.aod"
    ));
    private SharedPreferences sharedPreferences;

    private List<BlockedApp> appsLimitades = new ArrayList<>();
    public void setAppsLimitades(List<BlockedApp> list) { appsLimitades = list; }
    public void putAppLimitada(BlockedApp blockedApp) { appsLimitades.add(blockedApp); }
    public void removeAppLimitada(String pkgName){
        appsLimitades.removeAll(appsLimitades.stream()
                .filter(blockedApp -> blockedApp.pkgName.equals(pkgName))
                .collect(Collectors.toList()));
    }

    private Map<String, Long> tempsApps = new HashMap<>();
    public void setTempsApps(Map<String, Long> map) { tempsApps = map; }
    public void putTempsApp (String pkgName, Long time) { tempsApps.put(pkgName, time); }

    private long startAppTime = 0;
    private int ultimCopAcceditApp = 0;

    private List<String> blockedApps = new ArrayList<>();
    public void addBlockedApp(String app) {
        blockedApps.add(app);
        changedBlockedApps = true;
    }
    public void setBlockedApps(List<String> list) {
        blockedApps = list;
        changedBlockedApps = true;
    }
    public boolean isCurrentAppBlocked() {

        // Si estem a la pantalla de bloqueig d'apps
        if(Objects.equals(currentClassName, "com.adictic.client.ui.BlockAppActivity")){
            // Si l'últim package no esta bloquejat, acabem l'activitat
            if(!blockedApps.contains(lastPackage))
                LocalBroadcastManager.getInstance(AccessibilityScreenService.this).sendBroadcast(new Intent(Constants.NO_APP_BLOCK_SCREEN));

            return false;
        }
        else {
            boolean blocked = currentPackage != null && blockedApps.contains(currentPackage);
            if(blocked)
                Funcions.showBlockAppScreen(AccessibilityScreenService.this, currentPackage, currentAppName);

            return blocked;
        }
    }

    private String lastAppName = "";
    private String lastPackage = "";
    public String getLastPackage() { return lastPackage; }

    private String lastClassName = "";
    private String currentPackage = "";
    private String currentAppName = "";
    private String currentClassName = "";
    public String getCurrentPackage() { return currentPackage; }
    public String getCurrentAppName() { return currentAppName; }

    private final List<String> allowedApps = new ArrayList<>(Arrays.asList(
            "BlockDeviceActivity",
            "com.adictic.client.ui.BlockDeviceActivity",
            "com.android.contacts",
            "com.adictic.client"
    ));

    private final List<String> ignoreActivities = new ArrayList<>(Arrays.asList(
            "android.widget.FrameLayout",
            "BlockAppActivity"
    ));

    private boolean liveApp = false;
    public void setLiveApp(boolean bool) { liveApp = bool; }

    private boolean freeUse = false;
    public void setFreeUse(boolean bool) { freeUse = bool; }
    public boolean getFreeUse() { return  freeUse; }

    private boolean blockDevice = false;
    public void setBlockDevice(boolean bool) { blockDevice = bool; }

    private boolean changedBlockedApps = false;
    public void setChangedBlockedApps(boolean bool) { changedBlockedApps = bool; }

    private int activeEvents = 0;
    public void setActiveEvents(int i) { activeEvents = i; }
    public void addActiveEvent() { activeEvents++; }
    public void deleteActiveEvent() { activeEvents = Math.max(activeEvents--, 0); }

    private boolean horarisActius = false;
    public void setHorarisActius(boolean b) { horarisActius = b; }

    private int dailyLimitDevice = 0;
    private int dayUsage = 0;
    private boolean excessUsageDevice = false;
    public void setExcessUsageDevice(boolean b) { excessUsageDevice = b; }
    private long unlockedDeviceTime = -1;

    private long lastTimeChecked = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false)) {
            disableSelf();
        }
        else {
            registerScreenLockReceiver();
            fetchDades();

            excessUsageDevice = false;
            dayUsage = Math.toIntExact(Funcions.getGeneralUsages(AccessibilityScreenService.this, 0).get(0).totalTime);
            if(dailyLimitDevice > 0) {
                if(dayUsage > dailyLimitDevice)
                    excessUsageDevice = true;
            }
            unlockedDeviceTime = System.currentTimeMillis();
            lastTimeChecked = System.currentTimeMillis();

            lastAppName = "";
            lastPackage = "";

            //Configure these here for compatibility with API 13 and below.
            AccessibilityServiceInfo config = getServiceInfo();

            config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            setServiceInfo(config);
        }
    }

    public void updateDeviceBlock(){
        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
        if(myKM.isDeviceLocked())
            return;

        if(isDeviceBlocked()) {
            if(BlockDeviceActivity.instance == null || !BlockDeviceActivity.instance.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                Funcions.showBlockDeviceScreen(AccessibilityScreenService.this);
        }
        else {
            LocalBroadcastManager.getInstance(AccessibilityScreenService.this).sendBroadcast(new Intent(Constants.NO_DEVICE_BLOCK_SCREEN));
            isCurrentAppBlocked();
        }
    }

    public boolean isDeviceBlocked(){
        return !freeUse && (horarisActius || activeEvents > 0 || blockDevice || excessUsageDevice);
    }

    public void setLimitDevice(int limit){
        dailyLimitDevice = limit;
        if(limit == 0) {
            excessUsageDevice = false;
            updateDeviceBlock();
            cancelarWorkerBlockDevice();
        }
        else{
            // Si encara no hem utilitzat el dispositiu massa implementem el worker
            excessUsageDevice = dayUsage > dailyLimitDevice;
            if(!excessUsageDevice)
                programarWorkerBlockDevice(dailyLimitDevice - dayUsage);
            else
                updateDeviceBlock();
        }
    }

    private void fetchDades() {
        liveApp = sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP, false);
        freeUse = sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false);
        blockDevice = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false);
        dailyLimitDevice = sharedPreferences.getInt(Constants.SHARED_PREFS_DAILY_USAGE_LIMIT, 0);

        if(sharedPreferences.contains(Constants.SHARED_PREFS_IDUSER)) {
            AdicticApi mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
            long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Funcions.fetchAppBlockFromServer(AccessibilityScreenService.this);

            Funcions.checkEvents(AccessibilityScreenService.this);
            Funcions.checkHoraris(AccessibilityScreenService.this);

            Call<Boolean> call = mTodoService.getBlockStatus(idChild);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    super.onResponse(call, response);
                    if(response.isSuccessful() && response.body() != null){
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,response.body()).apply();
                        blockDevice = response.body();
                        if(Funcions.accessibilityServiceOn())
                            AccessibilityScreenService.instance.updateDeviceBlock();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });

            Call<Integer> dailyLimitCall = mTodoService.getDailyLimit(idChild);
            dailyLimitCall.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                    super.onResponse(call, response);
                    if(response.isSuccessful() && response.body() != null){
                        setLimitDevice(response.body());
                        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_DAILY_USAGE_LIMIT, response.body()).apply();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getClassName() != null &&
                !ignoreActivities.contains(event.getClassName().toString()) &&
                isActivity(event)) {

            updateDayVariables();

            currentPackage = event.getPackageName().toString();
            currentClassName = event.getClassName().toString();

            // Cronometrem el temps d'ús de l'app
            boolean samePkg = currentPackage.equals(lastPackage);
            if(!samePkg)
                cronometrarApp(currentPackage);

            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
            if(myKM.isDeviceLocked()) {
                currentPackage = "";
                return;
            }

            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(event.getPackageName().toString(), 0);
                currentAppName = appInfo.loadLabel(getPackageManager()).toString();
            } catch (PackageManager.NameNotFoundException e) {
                currentAppName = event.getPackageName().toString();
            }

            Log.d(TAG, "Nou 'package' sense canvis en bloquejos");
            boolean shouldDeviceBeBlocked = isDeviceBlocked();

            // --- LIVE APP ---
            if (liveApp && !shouldDeviceBeBlocked)
                enviarLiveApp(currentPackage, currentAppName);

            // --- FREE USE ---
            // Si és FreeUse, tornem sense fer res
            if (freeUse) {
                Log.d(TAG, "FreeUse Activat");
                return;
            }

            // --- BLOCK DEVICE ---
            String className = event.getClassName().toString();
            if (shouldDeviceBeBlocked && !myKM.isDeviceLocked() && !allowedApps.contains(className) && !allowedApps.contains(currentPackage)) {
                Funcions.showBlockDeviceScreen(AccessibilityScreenService.this);
                return;
            }

            //Mirem si l'app està bloquejada
            isCurrentAppBlocked();

            // --- TRACTAR APP ACTUAL ---
            lastPackage = currentPackage;
            lastAppName = currentAppName;
            lastClassName = currentClassName;
        }
    }

    private void updateDayVariables() {
        long now = System.currentTimeMillis();
        int millisOfDay = new DateTime().getMillisOfDay();
        // Hem canviat de dia
        if(lastTimeChecked < (now - millisOfDay)){
            // Ús del dispositiu a 0
            dayUsage = 0;

            // No hem excedit el temps d'ús
            excessUsageDevice = false;

            // Últim cop que hem obert el dispositiu avui
            unlockedDeviceTime = now;

            // L'ús de totes les apps el netegem
            tempsApps.clear();

            // Les apps limitades les treiem de bloquejades
            if(appsLimitades != null && !appsLimitades.isEmpty() && blockedApps != null) {
                blockedApps.removeAll(
                        appsLimitades.stream()
                                .map(blockedApp -> blockedApp.pkgName)
                                .collect(Collectors.toList())
                );
            }

            // Comencem a comptar app limitada ara
            startAppTime = System.currentTimeMillis();

            // Si l'app actual està limitada comencem el worker
            BlockedApp blockedApp = appsLimitades.stream()
                    .filter(blockedApp1 -> blockedApp1.pkgName.equals(currentPackage))
                    .findFirst()
                    .orElse(null);

            if(blockedApp == null)
                return;

            Long appTimeUsed = tempsApps.getOrDefault(currentPackage, 0L);

            long delay = appTimeUsed != null ? blockedApp.timeLimit - appTimeUsed : blockedApp.timeLimit;

            // Engegar worker amb delay blockedApp.timeBlocked - appTimeUsed
            programarWorkerBloqueigApp(currentPackage, delay);
        }

        lastTimeChecked = now;
    }

    @Override
    public void onInterrupt() { }

    private boolean isActivity(AccessibilityEvent event) {
        ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
        try {
            if(getPackageManager().getActivityInfo(componentName, 0) != null)
                return true;

            currentPackage = "";
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            currentPackage = "";
            return false;
        }
    }

    private void cronometrarApp(String currentPkg) {
        startAppTime = System.currentTimeMillis();
        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
        if((myKM.isDeviceLocked() && lastPackage.equals("")) || lastPackage.equals(""))
            return;

        // Cancel·lem el worker de bloqueig d'app
        cancelarWorkerBloqueigApp();

        long tempsUsLastApp = System.currentTimeMillis() - startAppTime;

        if(tempsApps.containsKey(lastPackage))
            tempsUsLastApp += tempsApps.getOrDefault(lastPackage, 0L);

        tempsApps.put(lastPackage, tempsUsLastApp);

        // Si l'app actual està limitada comencem el worker
        BlockedApp blockedApp = appsLimitades.stream()
                .filter(blockedApp1 -> blockedApp1.pkgName.equals(currentPkg))
                .findFirst()
                .orElse(null);

        if(blockedApp == null)
            return;

        Long appTimeUsed = tempsApps.getOrDefault(currentPkg, 0L);

        long delay = appTimeUsed != null ? blockedApp.timeLimit - appTimeUsed : blockedApp.timeLimit;

        // Engegar worker amb delay blockedApp.timeBlocked - appTimeUsed
        programarWorkerBloqueigApp(currentPkg, delay);
    }

    // *************** WORKERS ***************

    private void cancelarWorkerBlockDevice(){
        // Cancelem tots els workers que hi pugui haver
        WorkManager.getInstance(getApplicationContext())
                .cancelUniqueWork("bloqueigDeviceWorker");

        Log.d(TAG,"Worker bloqueigDeviceWorker cancel·lat");
    }

    private void programarWorkerBlockDevice(long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(BlockDeviceWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(AccessibilityScreenService.this)
                .enqueueUniqueWork("bloqueigDeviceWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker bloqueigDeviceWorker Començat");
    }

    private void cancelarWorkerBloqueigApp(){
        // Cancelem tots els workers que hi pugui haver
        WorkManager.getInstance(getApplicationContext())
                .cancelUniqueWork("bloqueigAppWorker");

        Log.d(TAG,"Worker bloqueigAppWorker (únic) cancel·lat");
    }

    private void programarWorkerBloqueigApp(String pkgName, long delay){
        Data.Builder data = new Data.Builder();
        data.putString("pkgName", pkgName);

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(BlockSingleAppWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(AccessibilityScreenService.this)
                .enqueueUniqueWork("bloqueigAppWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker bloqueigAppWorker (únic) Començat");
    }

    // ******************** END WORKERS ***********************

    public void enviarLiveApp(){
        enviarLiveApp(lastPackage, lastAppName);
    }

    private void enviarLiveApp(String pkgName, String appName) {
        LiveApp liveApp = new LiveApp();

        liveApp.pkgName = pkgName;
        liveApp.appName = appName;
        liveApp.time = DateTime.now().getMillis();

        if(sharedPreferences == null)
            sharedPreferences = Funcions.getEncryptedSharedPreferences(AccessibilityScreenService.this);

        Call<String> call = ((AdicticApp) getApplication()).getAPI().sendTutorLiveApp(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call,response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    private void registerScreenLockReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SCREEN_OFF);
        intentFilter.addAction(ACTION_SCREEN_ON);
        ScreenLockReceiver screenLockReceiver = new ScreenLockReceiver();
        AccessibilityScreenService.this.registerReceiver(screenLockReceiver, intentFilter);
    }

    public static class ScreenLockReceiver extends BroadcastReceiver {
        private boolean wasLocked = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            KeyguardManager myKM = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            if(intent.getAction().equals(ACTION_SCREEN_OFF) && !wasLocked){
                wasLocked = true;
                AccessibilityScreenService.instance.dayUsage += Math.abs(AccessibilityScreenService.instance.unlockedDeviceTime - System.currentTimeMillis());

                enviarLastApp();
            }
            else if(intent.getAction().equals(ACTION_SCREEN_ON) && (instance.freeUse || (!instance.blockDevice && instance.activeEvents == 0 && !instance.horarisActius && !instance.excessUsageDevice)) && !myKM.isDeviceLocked()) {
                wasLocked = false;
                AccessibilityScreenService.instance.unlockedDeviceTime = System.currentTimeMillis();

                // Mirem si hi ha límit establert
                if(AccessibilityScreenService.instance.dailyLimitDevice > 0){
                    int timeUntilLimit = AccessibilityScreenService.instance.dailyLimitDevice - AccessibilityScreenService.instance.dayUsage;
                    AccessibilityScreenService.instance.excessUsageDevice = timeUntilLimit < 0;
                    if(AccessibilityScreenService.instance.excessUsageDevice)
                        AccessibilityScreenService.instance.updateDeviceBlock();
                    else
                        AccessibilityScreenService.instance.programarWorkerBlockDevice(timeUntilLimit);
                }
                else
                    AccessibilityScreenService.instance.excessUsageDevice = false;
            }
        }

        private void enviarLastApp() {
            if(!Funcions.accessibilityServiceOn())
                return;

            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(AccessibilityScreenService.instance);
            assert sharedPreferences != null;

            LiveApp liveApp = new LiveApp();
            liveApp.pkgName = AccessibilityScreenService.instance.lastPackage;
            liveApp.appName = AccessibilityScreenService.instance.lastAppName;
            liveApp.time = DateTime.now().getMillis();

            // També actualitzem les dades d'ús al servidor
            try {
                if(WorkManager.getInstance(instance.getApplicationContext()).getWorkInfosByTag(Constants.WORKER_TAG_APP_USAGE).get().isEmpty())
                    Funcions.startAppUsageWorker24h(instance.getApplicationContext());
                else
                    Funcions.sendAppUsage(instance.getApplicationContext());

            } catch (ExecutionException | InterruptedException e) {
                Funcions.startAppUsageWorker24h(instance.getApplicationContext());
            }

            Call<String> call = ((AdicticApp) AccessibilityScreenService.instance.getApplicationContext()).getAPI().postLastAppUsed(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
            call.enqueue(new Callback<String>(){
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }
    }

}
