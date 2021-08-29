package com.example.adictic.service;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
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

import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.entity.LiveApp;
import com.adictic.common.util.Constants;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.ui.BlockedDevice;
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

    private String lastActivity;
    private String lastPackage;

    private boolean estavaBloquejatAbans = false;

    private final List<String> allowedApps = new ArrayList<>(Arrays.asList(
            "com.example.adictic.ui.BlockedDevice",
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

        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
            disableSelf();
        else {
            fetchDades();

            mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
            blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, true);

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

        Funcions.checkHoraris(getApplicationContext());
        Funcions.checkEvents(getApplicationContext());
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

            // Agafem info de l'Event
            ComponentName componentName = new ComponentName(
                    currentPackage,
                    className
            );

            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;

            // Si estem al mateix pkg i no hi ha hagut canvis a bloquejos d'apps i l'app no està bloquejada sortim
            if(lastPackage.equals(currentPackage) && !changedBlockedApps) {
//                if (blockDevice) {
////                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
////                    assert mDPM != null;
////                    mDPM.lockNow();
//                    Funcions.showBlockDeviceScreen(AccessibilityScreenService.this);
//
//                    if (liveApp) {
//                        enviarLiveApp(lastPackage, lastActivity);
//                    }
//                }
                return;
            }

            Log.d(TAG, "Nou 'package' sense canvis en bloquejos");

            // --- LIVE APP ---
            if (isActivity && liveApp && !blockDevice) {
                String pkgName = lastPackage;
                String appName = lastActivity;
                if(!blackListLiveApp.contains(event.getPackageName().toString())) {
                    pkgName = currentPackage;
                    appName = currentAppName;
                }
                enviarLiveApp(pkgName, appName);
            }

            // --- FREE USE ---
            // Si és FreeUse, tornem sense fer res
            if (freeUse) {
                Log.d(TAG, "FreeUse Activat");
                return;
            }

            // --- BLOCK DEVICE ---
            if (blockDevice) {
                if(estavaBloquejatAbans && !currentPackage.equals(lastPackage))
                    postIntentAccesDisp();
                estavaBloquejatAbans = true;
                if (!myKM.isDeviceLocked() && !sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false)) {
                    Log.d(TAG, "Dispositiu bloquejat");
                    showBlockedDeviceScreen();
                }
                return;
            }

            // --- TRACTAR APP ACTUAL ---
            // Només entrem a fer coses si s'han canviat les apps bloquejades o el pkgName és diferent a l'anterior (per evitar activitats) I dispositiu no està bloquejat
            if(changedBlockedApps || !currentPackage.equals(lastPackage)) {
                lastPackage = currentPackage;
                Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

                if (changedBlockedApps)
                    blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, true);

                //
                if (event.getPackageName() != null && event.getClassName() != null) {
                    Log.d(TAG, "L'event és una activitat");
                    lastActivity = currentAppName;

                    //Mirem si l'app està bloquejada
                    boolean isBlocked = false;
                    if(blockedApps != null)
                        isBlocked = blockedApps.contains(lastPackage);

                    if (isBlocked) {
                        addAccessBlockedApp();
                        Funcions.showBlockAppScreen(getApplicationContext(), lastPackage);
                    }
                }
            }

            // Actualitzem variable estavaBloquejatAbans
            estavaBloquejatAbans = myKM.isDeviceLocked();
        }
    }

    private void postIntentAccesDisp() {
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idChild == -1)
            return;

        retryCountAccessDisp = 0;
        long now = DateTime.now().getMillis();
        Call<String> call = mTodoService.postIntentAccesDisp(idChild, now);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(!response.isSuccessful() && retryCountAccessDisp++ < TOTAL_RETRIES)
                    Funcions.retryFailedCall(this, call, 5000);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if(retryCountAccessDisp++ < TOTAL_RETRIES)
                    Funcions.retryFailedCall(this, call, 5000);
            }
        });
    }

    private void addAccessBlockedApp(){
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idChild == -1)
            return;

        IntentsAccesApp intentsAccesApp = new IntentsAccesApp();
        intentsAccesApp.appName = lastActivity;
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
        enviarLiveApp(lastPackage, lastActivity);
    }

    private void enviarLiveApp(String pkgName, String appName) {
        LiveApp liveApp = new LiveApp();
        if(estavaBloquejatAbans)
            liveApp.pkgName = "-1";
        else
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

    public static class ScreenLockReceiver extends BroadcastReceiver {
        private int retryCountLastApp = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                enviarLastApp();
            }
        }

        private void enviarLastApp() {
            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(AccessibilityScreenService.instance);
            assert sharedPreferences != null;

            LiveApp liveApp = new LiveApp();
            liveApp.pkgName = AccessibilityScreenService.instance.lastPackage;
            liveApp.appName = AccessibilityScreenService.instance.lastActivity;
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
        Log.d(TAG,"Creant Intent cap a BlockScreenActivity");
        Intent lockIntent = new Intent(AccessibilityScreenService.this, BlockedDevice.class);
        lockIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        lockIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        AccessibilityScreenService.this.startActivity(lockIntent);
    }

}
