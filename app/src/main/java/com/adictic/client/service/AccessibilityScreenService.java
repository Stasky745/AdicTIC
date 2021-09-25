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

    private Map<String, Integer> tempsAppsLimitades = new HashMap<>();
    public void setTempsAppsLimitades (Map<String, Integer> map) { tempsAppsLimitades = map; }
    public void putTempsAppLimitada (String pkgName, Integer time) { tempsAppsLimitades.put(pkgName, time); }

    private int startAppLimitada = 0;
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
        return currentPackage != null && blockedApps.contains(currentPackage);
    }

    private String lastAppName = "";
    private String lastPackage = "";
    private String currentPackage = "";
    private String currentAppName = "";
    public String getCurrentPackage() { return currentPackage; }
    public String getCurrentAppName() { return currentAppName; }

    private final List<String> allowedApps = new ArrayList<>(Arrays.asList(
            "BlockDeviceActivity",
            "com.adictic.client.ui.BlockDeviceActivity",
            "com.android.contacts.activities.PeopleActivity"
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

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

        registerScreenLockReceiver();

        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
            disableSelf();
        else {
            fetchDades();

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

        boolean deviceBlocked = !freeUse && (horarisActius || activeEvents > 0 || blockDevice || isCurrentAppBlocked());

        if(deviceBlocked) {
            if(BlockDeviceActivity.instance == null || !BlockDeviceActivity.instance.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                Funcions.showBlockDeviceScreen(AccessibilityScreenService.this);
        }
        else
            LocalBroadcastManager.getInstance(AccessibilityScreenService.this).sendBroadcast(new Intent(Constants.NO_BLOCK_SCREEN));
    }

    public boolean isDeviceBlocked(){
        return !freeUse && (horarisActius || activeEvents > 0 || blockDevice || isCurrentAppBlocked());
    }

    private void fetchDades() {
        liveApp = sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP, false);
        freeUse = sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false);
        blockDevice = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false);

        if(sharedPreferences.contains(Constants.SHARED_PREFS_IDUSER)) {
            AdicticApi mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
            long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Funcions.fetchAppBlockFromServer(AccessibilityScreenService.this);

            Funcions.checkEvents(AccessibilityScreenService.this);
            Funcions.checkHoraris(AccessibilityScreenService.this);

            Call<Boolean> call2 = mTodoService.getBlockStatus(idChild);
            call2.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    super.onResponse(call, response);
                    if(response.isSuccessful() && response.body() != null){
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,response.body()).apply();
                        if(Funcions.accessibilityServiceOn())
                            AccessibilityScreenService.instance.updateDeviceBlock();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
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

            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
            if(myKM.isDeviceLocked()) {
                currentPackage = "";
                return;
            }

            currentPackage = event.getPackageName().toString();
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(event.getPackageName().toString(), 0);
                currentAppName = appInfo.loadLabel(getPackageManager()).toString();
            } catch (PackageManager.NameNotFoundException e) {
                currentAppName = event.getPackageName().toString();
            }

            boolean samePkg = currentPackage.equals(lastPackage);

            // Reset al mapa de temps si és un dia nou
            int today = DateTime.now().get(DateTimeFieldType.dayOfYear());
            if(ultimCopAcceditApp != today || ultimCopAcceditApp == 0){
                ultimCopAcceditApp = today;
                tempsAppsLimitades.clear();

//                blockedApps = Funcions.readFromFile(AccessibilityScreenService.this, Constants.FILE_BLOCKED_APPS, true);
//                Funcions.write2File(AccessibilityScreenService.this, Constants.FILE_CURRENT_BLOCKED_APPS, blockedApps);
            }

            // Cronometrem el temps d'ús de l'app si està bloquejada
            if(!samePkg)
                cronometrarAppBloquejada(currentPackage);

            // Si estem al mateix pkg i no hi ha hagut canvis a bloquejos d'apps i l'app no està bloquejada sortim
            if(samePkg && !changedBlockedApps)
                return;

            changedBlockedApps = false;
            Log.d(TAG, "Nou 'package' sense canvis en bloquejos");
            boolean shouldDeviceBeBlocked = !freeUse && (blockDevice || activeEvents > 0 || horarisActius);

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
            if (shouldDeviceBeBlocked && !myKM.isDeviceLocked() && !allowedApps.contains(className)) {
                Funcions.showBlockDeviceScreen(AccessibilityScreenService.this);
                return;
            }

            // --- TRACTAR APP ACTUAL ---
            lastPackage = currentPackage;
            lastAppName = currentAppName;

            //Mirem si l'app està bloquejada
            boolean isBlocked = false;
            if(blockedApps != null && !blockedApps.isEmpty())
                isBlocked = blockedApps.contains(lastPackage);

            if (isBlocked)
                Funcions.showBlockAppScreen(getApplicationContext(), lastPackage, lastAppName);
        }
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

    private void cronometrarAppBloquejada(String currentPkg) {
        // Si no hi ha apps limitades, sortim
        if(appsLimitades == null || appsLimitades.isEmpty()) {
            startAppLimitada = 0;
            return;
        }

        // Si startAppLimitada és superior a 0, lastPackage està limitat. Actualitzem
        if(startAppLimitada > 0){
            int tempsUsAppLimitada = DateTime.now().getMillisOfDay() - startAppLimitada;

            if(tempsAppsLimitades.containsKey(lastPackage))
                tempsUsAppLimitada += tempsAppsLimitades.getOrDefault(lastPackage, 0);

            tempsAppsLimitades.put(lastPackage, tempsUsAppLimitada);

            // Cancel·lem el worker de bloqueig d'app
            cancelarWorkerBloqueigApp();
        }

        startAppLimitada = 0;

        // Si l'app actual està bloquejada, tornem
        if(blockedApps != null && blockedApps.contains(currentPkg))
            return;

        // Si l'app actual està limitada, comencem a comptar. Sinó tornem
        BlockedApp blockedApp = appsLimitades.stream()
                .filter(blockedApp1 -> blockedApp1.pkgName.equals(currentPkg))
                .findFirst()
                .orElse(null);

        if(blockedApp == null)
            return;

        startAppLimitada = DateTime.now().getMillisOfDay();

        Integer appTimeUsed = tempsAppsLimitades.getOrDefault(currentPkg, 0);

        long delay = appTimeUsed != null ? blockedApp.timeLimit - appTimeUsed : blockedApp.timeLimit;

        // Engegar worker amb delay blockedApp.timeBlocked - appTimeUsed
        programarWorkerBloqueigApp(currentPkg, delay);
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
                enviarLastApp();
            }
            else if(intent.getAction().equals(ACTION_SCREEN_ON) && (instance.freeUse || (!instance.blockDevice && instance.activeEvents == 0 && !instance.horarisActius)) && !myKM.isDeviceLocked())
                wasLocked = false;
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
            Funcions.startAppUsageWorker24h(instance.getApplicationContext());
            Funcions.sendAppUsage(instance.getApplicationContext());

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
