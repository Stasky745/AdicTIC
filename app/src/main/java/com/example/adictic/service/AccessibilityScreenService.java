package com.example.adictic.service;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.entity.LiveApp;
import com.adictic.common.util.Constants;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.ui.BlockDeviceActivity;
import com.example.adictic.util.AdicticApp;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccessibilityScreenService extends AccessibilityService {

    public static AccessibilityScreenService instance;

    private final static int TOTAL_RETRIES = 10;
    private int retryCountAccessDisp = 0;
    private int retryCountAccessApp = 0;

    private static final String TAG = AccessibilityScreenService.class.getSimpleName();
    private final List<String> blackListLiveApp = new ArrayList<>(Arrays.asList(
            "com.google.android.apps.nexuslauncher",
            "com.android.systemui",
            "com.miui.aod"
    ));
    private AdicticApi mTodoService;
    private SharedPreferences sharedPreferences;

    private List<String> blockedApps;

    private String lastAppName;
    private String lastPackage;

    private final List<String> allowedApps = new ArrayList<>(Arrays.asList(
            "com.example.adictic.ui.BlockDeviceActivity",
            "com.android.contacts.activities.PeopleActivity"
    ));

    private final List<String> ignoreActivities = new ArrayList<>(Arrays.asList(
            "android.widget.FrameLayout"
    ));

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

            mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
            blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, true);

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

    private void fetchDades() {
        mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
        if(sharedPreferences.contains(Constants.SHARED_PREFS_IDUSER)) {
            long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Call<BlockedLimitedLists> call = mTodoService.getBlockedLimitedLists(idChild);
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

            Funcions.checkEvents(getApplicationContext());
            Funcions.checkHoraris(getApplicationContext());

            Call<Boolean> call2 = mTodoService.getBlockStatus(idChild);
            call2.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                    if(response.isSuccessful() && response.body() != null){
                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,response.body()).apply();
                        Funcions.endFreeUse(getApplicationContext());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {

                }
            });
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            sharedPreferences = Funcions.getEncryptedSharedPreferences(AccessibilityScreenService.this);
            assert sharedPreferences != null;

            String className = event.getClassName().toString();

            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);

            if(myKM.isDeviceLocked() || ignoreActivities.contains(className))
                return;

            boolean liveApp = sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP, false);
            boolean freeUse = sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false);
            boolean blockDevice = !freeUse && estaBloquejat();
            boolean changedBlockedApps = sharedPreferences.getBoolean(Constants.SHARED_PREFS_CHANGE_BLOCKED_APPS,false);

            String currentPackage = event.getPackageName().toString();
            String currentAppName;
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(event.getPackageName().toString(), 0);
                currentAppName = appInfo.loadLabel(getPackageManager()).toString();
            } catch (PackageManager.NameNotFoundException e) {
                currentAppName = event.getPackageName().toString();
            }

            // Si és una activitat permesa durant bloqueig, posar 'blockDevice' a false
            if(allowedApps.contains(className))
                blockDevice = false;

            // Si estem al mateix pkg i no hi ha hagut canvis a bloquejos d'apps i l'app no està bloquejada sortim
            if(lastPackage.equals(currentPackage) && !changedBlockedApps)
                return;

            Log.d(TAG, "Nou 'package' sense canvis en bloquejos");

            // --- LIVE APP ---
            if (liveApp && !blockDevice) {
//                String pkgName = lastPackage;
//                String appName = lastActivity;
//                if(!blackListLiveApp.contains(event.getPackageName().toString())) {
//                    pkgName = currentPackage;
//                    appName = currentAppName;
//                }
//                enviarLiveApp(pkgName, appName);
                enviarLiveApp(currentPackage, currentAppName);
            }

            // --- FREE USE ---
            // Si és FreeUse, tornem sense fer res
            if (freeUse) {
                Log.d(TAG, "FreeUse Activat");
                return;
            }

            // --- BLOCK DEVICE ---
            if (blockDevice && !myKM.isDeviceLocked()) {
                showBlockedDeviceScreen();
                return;
            }

            // --- TRACTAR APP ACTUAL ---
            // Només entrem a fer coses si s'han canviat les apps bloquejades o el pkgName és diferent a l'anterior (per evitar activitats) I dispositiu no està bloquejat
            if(changedBlockedApps || !currentPackage.equals(lastPackage)) {
                lastPackage = currentPackage;
                lastAppName = currentAppName;
                Log.d(TAG, "Window State Changed - Event: " + currentPackage);

                if (changedBlockedApps) {
                    blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, true);
                }

                //Mirem si l'app està bloquejada
                boolean isBlocked = false;
                if(blockedApps != null && !blockedApps.isEmpty())
                    isBlocked = blockedApps.contains(lastPackage);

                if (isBlocked) {
                    addAccessBlockedApp();
                    Funcions.showBlockAppScreen(getApplicationContext(), lastPackage, lastAppName);
                }
            }
        }
    }

    private void addAccessBlockedApp(){
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idChild == -1)
            return;

        IntentsAccesApp intentsAccesApp = new IntentsAccesApp();
        intentsAccesApp.appName = lastAppName;
        intentsAccesApp.pkgName = lastPackage;
        intentsAccesApp.data = DateTime.now().getMillis();
        retryCountAccessApp = 0;
        Call<String> call = mTodoService.postIntentAccesApp(idChild, intentsAccesApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(!response.isSuccessful() && retryCountAccessApp++ < TOTAL_RETRIES)
                    Funcions.retryFailedCall(this, call, 5000);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if(retryCountAccessApp++ < TOTAL_RETRIES)
                    Funcions.retryFailedCall(this, call, 5000);
            }
        });
    }

    private boolean estaBloquejat() {
        return sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false)
                || sharedPreferences.getBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT,false)
                || sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, 0) > 0;
    }

    public void enviarLiveApp(){
        enviarLiveApp(lastPackage, lastAppName);
    }

    private void enviarLiveApp(String pkgName, String appName) {
        LiveApp liveApp = new LiveApp();

        liveApp.pkgName = pkgName;
        liveApp.appName = appName;
        liveApp.time = Calendar.getInstance().getTimeInMillis();

        Call<String> call = ((AdicticApp) getApplication()).getAPI().sendTutorLiveApp(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
        });
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

    private void registerScreenLockReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_SCREEN_OFF);
        intentFilter.addAction(ACTION_SCREEN_ON);
        ScreenLockReceiver screenLockReceiver = new ScreenLockReceiver();
        AccessibilityScreenService.this.registerReceiver(screenLockReceiver, intentFilter);
    }

    public static class ScreenLockReceiver extends BroadcastReceiver {
        private int retryCountLastApp = 0;
        private boolean wasLocked = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            KeyguardManager myKM = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            if(intent.getAction().equals(ACTION_SCREEN_OFF) && !wasLocked){
                wasLocked = true;
                enviarLastApp();
            }
            else if(intent.getAction().equals(ACTION_SCREEN_ON) && !myKM.isDeviceLocked())
                wasLocked = false;
        }

        private void enviarLastApp() {
            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(AccessibilityScreenService.instance);
            assert sharedPreferences != null;

            LiveApp liveApp = new LiveApp();
            liveApp.pkgName = AccessibilityScreenService.instance.lastPackage;
            liveApp.appName = AccessibilityScreenService.instance.lastAppName;
            liveApp.time = Calendar.getInstance().getTimeInMillis();

            retryCountLastApp = 0;

            // També actualitzem les dades d'ús al servidor
            Funcions.runUniqueAppUsageWorker(AccessibilityScreenService.instance);

            Call<String> call = ((AdicticApp) AccessibilityScreenService.instance.getApplicationContext()).getAPI().postLastAppUsed(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if(!response.isSuccessful() && retryCountLastApp++ < 5)
                        Funcions.retryFailedCall(this, call, 2000);

                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    if(retryCountLastApp++ < 5)
                        Funcions.retryFailedCall(this, call, 2000);
                }
            });
        }
    }

    private void showBlockedDeviceScreen(){
        Log.d(TAG,"Creant Intent cap a BlockAppActivity");
        Intent lockIntent = new Intent(AccessibilityScreenService.this, BlockDeviceActivity.class);
        lockIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        lockIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        AccessibilityScreenService.this.startActivity(lockIntent);
    }

}
