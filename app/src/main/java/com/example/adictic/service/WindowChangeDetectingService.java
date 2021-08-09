package com.example.adictic.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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

public class WindowChangeDetectingService extends AccessibilityService {

    public static WindowChangeDetectingService instance;

    private final static int TOTAL_RETRIES = 10;
    private int retryCount = 0;

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
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
            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(KEYGUARD_SERVICE);
            boolean liveApp = sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP, false);
            boolean blockedDevice = estaBloquejat();
            boolean freeUse = sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false);
            boolean changedBlockedApps = sharedPreferences.getBoolean(Constants.SHARED_PREFS_CHANGE_BLOCKED_APPS,false);

            // Agafem info de l'Event
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;

            // Si estem al mateix pkg i no hi ha hagut canvis a bloquejos d'apps i l'app no està bloquejada sortim
            if(lastPackage.equals(event.getPackageName().toString()) && !changedBlockedApps) {
                if (!freeUse && blockedDevice) {
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    assert mDPM != null;
                    mDPM.lockNow();

                    if (liveApp) {
                        enviarLiveApp(lastPackage, lastActivity);
                    }
                    enviarLastApp();
                }
                return;
            }

            Log.d(TAG, "Nou 'package' sense canvis en bloquejos");

            // --- LIVE APP ---
            // Enviem l'última app oberta a la mare si el dispositiu s'ha bloquejat
            if (myKM.isDeviceLocked() && !estavaBloquejatAbans) {
                estavaBloquejatAbans = true;
                if (liveApp) {
                    enviarLiveApp(lastPackage, lastActivity);
                }
                enviarLastApp();

                // També actualitzem les dades d'ús al servidor
                Funcions.runUniqueAppUsageWorker(getApplicationContext());
            }
            else if (isActivity && liveApp && !blockedDevice) {
                String pkgName = lastPackage;
                String appName = lastActivity;
                if(!blackListLiveApp.contains(event.getPackageName().toString())) {
                    pkgName = event.getPackageName().toString();
                    try {
                        ApplicationInfo appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                        appName = appInfo.loadLabel(getPackageManager()).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        appName = componentName.getPackageName();
                    }
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
            if (blockedDevice) {
                if(estavaBloquejatAbans)
                    postIntentAccesDisp();
                estavaBloquejatAbans = true;
                if (!myKM.isDeviceLocked() && !sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE, false)) {
                    Log.d(TAG, "Dispositiu bloquejat");
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    assert mDPM != null;
                    mDPM.lockNow();
                }
                return;
            }

            // --- TRACTAR APP ACTUAL ---
            // Només entrem a fer coses si s'han canviat les apps bloquejades o el pkgName és diferent a l'anterior (per evitar activitats) I dispositiu no està bloquejat
            if(changedBlockedApps || !event.getPackageName().toString().equals(lastPackage)) {
                lastPackage = event.getPackageName().toString();
                Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

                if (changedBlockedApps)
                    blockedApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_CURRENT_BLOCKED_APPS, true);

                //
                if (event.getPackageName() != null && event.getClassName() != null) {
                    Log.d(TAG, "L'event no és null - Entra a 'else if'");

                    Log.d(TAG, "L'event és una activitat");
                    ApplicationInfo appInfo;

                    try {
                        appInfo = getPackageManager().getApplicationInfo(componentName.getPackageName(), 0);
                        lastActivity = appInfo.loadLabel(getPackageManager()).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        lastActivity = componentName.getPackageName();
                    }
                    Log.d(TAG, "Llista Apps Bloquejades : " + blockedApps);
                    Log.d(TAG, "CurrentActivity : " + componentName.flattenToShortString());
                    Log.d(TAG, "CurrentPackage : " + lastPackage);

                    //Mirem si l'app està bloquejada
                    boolean isBlocked = false;
                    if(blockedApps != null)
                        isBlocked = blockedApps.contains(lastPackage);

                    if (isBlocked) {
                        if(estavaBloquejatAbans)
                            addAccessBlockedApp();
                        Funcions.ensenyarBlockScreenActivity(getApplicationContext(), lastPackage);
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

        retryCount = 0;
        long now = DateTime.now().getMillis();
        Call<String> call = mTodoService.postIntentAccesDisp(idChild, now);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(!response.isSuccessful() && retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if(retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
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
        retryCount = 0;
        Call<String> call = mTodoService.postIntentAccesApp(idChild, intentsAccesApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(!response.isSuccessful() && retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if(retryCount++ < TOTAL_RETRIES)
                    call.clone().enqueue(this);
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

    private void enviarLastApp() {
        LiveApp liveApp = new LiveApp();
        liveApp.pkgName = lastPackage;
        liveApp.appName = lastActivity;
        liveApp.time = Calendar.getInstance().getTimeInMillis();

        Call<String> call = ((AdicticApp) getApplication()).getAPI().postLastAppUsed(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
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

}
