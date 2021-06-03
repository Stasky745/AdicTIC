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
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.adictic.R;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.BlockScreenActivity;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class WindowChangeDetectingService extends AccessibilityService {

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
    private final List<String> blackListLiveApp = Collections.singletonList("com.google.android.apps.nexuslauncher");
    private TodoApi mTodoService;
    private SharedPreferences sharedPreferences;

    private List<String> blockedApps;

    private View floatyView;
    private WindowManager windowManager;

    private String lastActivity;
    private String lastPackage;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        assert sharedPreferences != null;
        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
            disableSelf();
        else {
            fetchDades();

            mTodoService = ((TodoApp) getApplicationContext()).getAPI();
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
            Log.d(TAG, "Window State Changed - Event: " + event.getPackageName());

            // Si és FreeUse, tornem sense fer res
            if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_FREEUSE,false)){
                Log.d(TAG, "FreeUse, return.");
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
            boolean estaBloquejat = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE,false)
                    || sharedPreferences.getBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT,false);

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

                        Log.i("LiveApp", sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP,false) +
                                "   idChild: " + sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));

                        if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_LIVEAPP,false)) {
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

        Call<String> call = ((TodoApp) getApplication()).getAPI().sendTutorLiveApp(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
        });
    }

    private void ensenyarBlockScreenActivity() {
        Call<String> call = mTodoService.callBlockedApp(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), lastPackage);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) { }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
        });

        // Si és MIUI
        try {
            if(Funcions.isXiaomi())
                addOverlayView();
            else{
                Log.d(TAG,"Creant Intent cap a BlockScreenActivity");
                Intent lockIntent = new Intent(WindowChangeDetectingService.this, BlockScreenActivity.class);
                lockIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                lockIntent.putExtra("pkgName",lastPackage);
                startActivity(lockIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Call<String> call = ((TodoApp) getApplication()).getAPI().postLastAppUsed(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), liveApp);
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
            else
                blockedApps = llista.stream()
                        .map(blockedApp -> blockedApp.pkgName)
                        .collect(Collectors.toList());
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

    private void addOverlayView() {

        final WindowManager.LayoutParams params;
        int layoutParamsType;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutParamsType,
                0,
                PixelFormat.OPAQUE);

        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        FrameLayout interceptorLayout = new FrameLayout(this) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {

                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    // Check if the HOME button is pressed
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

                        Log.v(TAG, "BACK Button Pressed");

                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
                        return true;
                    }
                }

                // Otherwise don't intercept the event
                return super.dispatchKeyEvent(event);
            }
        };

        LayoutInflater inflater = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        if (inflater != null) {
            floatyView = inflater.inflate(R.layout.block_layout, interceptorLayout);
            windowManager.addView(floatyView, params);

            Button BT_sortir = floatyView.findViewById(R.id.btn_sortir);
            BT_sortir.setOnClickListener(view -> {
                Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
                startHomescreen.addCategory(Intent.CATEGORY_HOME);
                startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startHomescreen);
                windowManager.removeView(floatyView);
            });
        }
        else {
            Log.e("SAW-example", "Layout Inflater Service is null; can't inflate and display R.layout.floating_view");
        }
    }

}
