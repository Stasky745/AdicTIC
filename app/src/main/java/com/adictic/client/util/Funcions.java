package com.adictic.client.util;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.security.crypto.EncryptedFile;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.adictic.client.R;
import com.adictic.client.entity.BlockedApp;
import com.adictic.client.entity.NotificationInformation;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.ui.BlockAppActivity;
import com.adictic.client.ui.BlockDeviceActivity;
import com.adictic.client.workers.AppUsageWorker;
import com.adictic.client.workers.EventWorker;
import com.adictic.client.workers.GeoLocWorker;
import com.adictic.client.workers.HorarisEventsWorkerManager;
import com.adictic.client.workers.HorarisWorker;
import com.adictic.client.workers.NotifWorker;
import com.adictic.client.workers.ServiceWorker;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.EventsAPI;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.entity.LimitedApps;
import com.adictic.common.rest.Api;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class Funcions extends com.adictic.common.util.Funcions {
    private final static String TAG = "Funcions";

    public static void addOverlayView(Context ctx, boolean blockApp) {

        final WindowManager.LayoutParams params;
        int layoutParamsType;

        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

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
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        FrameLayout interceptorLayout = new FrameLayout(ctx) {

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

        LayoutInflater inflater = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        if (inflater != null) {
            if (!blockApp){
                View floatyView = inflater.inflate(R.layout.block_layout, interceptorLayout);
                windowManager.addView(floatyView, params);

                Button BT_sortir = floatyView.findViewById(R.id.btn_sortir);
                BT_sortir.setOnClickListener(view -> {
                    Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
                    startHomescreen.addCategory(Intent.CATEGORY_HOME);
                    startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(startHomescreen);
                    windowManager.removeView(floatyView);
                });
            }
            else{
                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);
                assert sharedPreferences != null;

                boolean blockedDevice = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE, false);

                View floatyView = inflater.inflate(R.layout.block_device_layout, interceptorLayout);
                windowManager.addView(floatyView, params);

                TextView blockDeviceTitle = floatyView.findViewById(R.id.TV_block_device_title);
                blockDeviceTitle.setText(ctx.getString(R.string.locked_device));

                if (!blockedDevice){
                    List<EventBlock> eventsList = readFromFile(ctx, Constants.FILE_EVENT_BLOCK, false);
                    assert eventsList != null;
                    EventBlock eventBlock = eventsList.stream()
                            .filter(com.adictic.common.util.Funcions::eventBlockIsActive)
                            .findFirst()
                            .orElse(null);

                    if(eventBlock != null){
                        String title = eventBlock.name + "\n";
                        title += millis2horaString(ctx, eventBlock.startEvent) + " - " + millis2horaString(ctx, eventBlock.endEvent);
                        blockDeviceTitle.setText(title);
                    }
                }

                ConstraintLayout CL_device_blocked_call = floatyView.findViewById(R.id.CL_block_device_emergency_call);
                CL_device_blocked_call.setOnClickListener(view -> {
                    Uri number = Uri.parse("tel:" + 112);
                    Intent dial = new Intent(Intent.ACTION_CALL, number);
                    ctx.startActivity(dial);
                });
            }
        }
        else {
            Log.e("SAW-example", "Layout Inflater Service is null; can't inflate and display R.layout.floating_view");
        }
    }

    public static void checkEvents(Context ctx) {
        Log.d(TAG,"Check Events");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);
        assert sharedPreferences != null;

        Api mTodoService = ((AdicticApp) (ctx.getApplicationContext())).getAPI();

        // Agafem els horaris de la nit i Events
        Call<EventsAPI> call = mTodoService.getEvents(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<EventsAPI>() {
            @Override
            public void onResponse(@NonNull Call<EventsAPI> call, @NonNull Response<EventsAPI> response) {
                if (response.isSuccessful()) {
                    startHorarisEventsManagerWorker(ctx);

                    EventsAPI eventsAPI = response.body() != null ? response.body() : new EventsAPI();

                    eventsAPI.events = eventsAPI.events != null ? eventsAPI.events : new ArrayList<>();

                    write2File(ctx, Constants.FILE_EVENT_BLOCK, eventsAPI.events);
                    setEvents(ctx, new ArrayList<>(eventsAPI.events));
                }
                else {
                    List<EventBlock> list = Funcions.readFromFile(ctx, Constants.FILE_EVENT_BLOCK, false);
                    setEvents(ctx, list);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventsAPI> call, @NonNull Throwable t) {
                List<EventBlock> list = Funcions.readFromFile(ctx, Constants.FILE_EVENT_BLOCK, false);
                setEvents(ctx, list);
            }
        });
    }

    public static void setEvents(Context ctx, List<EventBlock> events) {
        // Aturem tots els workers d'Events que estiguin configurats
        WorkManager.getInstance(ctx)
                .cancelAllWorkByTag(Constants.WORKER_TAG_EVENT_BLOCK);

        if(!Funcions.accessibilityServiceOn(ctx)) {
            return;
        }

        AccessibilityScreenService.instance.setActiveEvents(0);

        if(events == null || events.isEmpty())
            return;

        int diaSetmana = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        List<EventBlock> eventsTodayList = new ArrayList<>();

        switch (diaSetmana) {
            case 1:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.sunday).collect(Collectors.toList());
                break;
            case 2:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.monday).collect(Collectors.toList());
                break;
            case 3:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.tuesday).collect(Collectors.toList());
                break;
            case 4:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.wednesday).collect(Collectors.toList());
                break;
            case 5:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.thursday).collect(Collectors.toList());
                break;
            case 6:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.friday).collect(Collectors.toList());
                break;
            case 7:
                eventsTodayList = events.stream().filter(eventBlock -> eventBlock.saturday).collect(Collectors.toList());
                break;
        }

        setEventWorkerByDay(ctx, eventsTodayList);
    }

    private static void setEventWorkerByDay(Context ctx, List<EventBlock> eventList) {
        eventList.sort(Comparator.comparingInt(eventBlock -> eventBlock.startEvent));

        // Mirem quins workers són necessaris en cas que hi hagi events que es solapin
        List<Pair<Integer, Integer>> workersList = new ArrayList<>();
        for(EventBlock event : eventList){
            Pair<Integer, Integer> newTime = new Pair<>(event.startEvent, event.endEvent);

            if(workersList.isEmpty())
                workersList.add(newTime);
            else {
                int index = workersList.size()-1;
                Pair<Integer, Integer> lastTime = workersList.get(index);
                if(newTime.first < lastTime.second && newTime.second > lastTime.second)
                    workersList.set(index, new Pair<>(lastTime.first, newTime.second));
                else if(newTime.first > lastTime.second)
                    workersList.add(newTime);
            }
        }

        // Fer workers
        for(Pair<Integer, Integer> pair : workersList){
            long now = DateTime.now().getMillisOfDay();

            long startTimeDelay = pair.first - now;
            long endTimeDelay = pair.second - now;

            if(startTimeDelay > 0) {
                setUpEventWorker(ctx, startTimeDelay, true);
                setUpEventWorker(ctx, endTimeDelay, false);
            }
            else if(endTimeDelay > 0){
                setUpEventWorker(ctx, endTimeDelay, false);

                if(Funcions.accessibilityServiceOn(ctx))
                    AccessibilityScreenService.instance.setActiveEvents(1);
            }
        }

        if(Funcions.accessibilityServiceOn(ctx))
            AccessibilityScreenService.instance.updateDeviceBlock();
    }

    public static void checkHoraris(Context ctx) {
        Log.d(TAG,"Check Horaris");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);
        assert sharedPreferences != null;

        Api mTodoService = ((AdicticApp) (ctx.getApplicationContext())).getAPI();

        // Agafem els horaris de la nit i Events
        Call<HorarisAPI> call = mTodoService.getHoraris(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<HorarisAPI>() {
            @Override
            public void onResponse(@NonNull Call<HorarisAPI> call, @NonNull Response<HorarisAPI> response) {
                if (response.isSuccessful()) {
                    startHorarisEventsManagerWorker(ctx);

                    HorarisAPI horarisAPI = response.body() != null ? response.body() : new HorarisAPI();
                    horarisAPI.horarisNit = horarisAPI.horarisNit != null ? new ArrayList<>(horarisAPI.horarisNit) : new ArrayList<>();
                    horarisAPI.tipus = horarisAPI.tipus != null ? horarisAPI.tipus : 1;

                    write2File(ctx, Constants.FILE_HORARIS_NIT, new ArrayList<>(horarisAPI.horarisNit));
                    setHoraris(ctx, new ArrayList<>(horarisAPI.horarisNit));
                }
                else {
                    ArrayList<HorarisNit> horarisNit = new ArrayList<>(Objects.requireNonNull(Funcions.readFromFile(ctx, Constants.FILE_HORARIS_NIT, false)));
                    setHoraris(ctx, horarisNit);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HorarisAPI> call, @NonNull Throwable t) {
                ArrayList<HorarisNit> horarisNit = new ArrayList<>(Objects.requireNonNull(Funcions.readFromFile(ctx, Constants.FILE_HORARIS_NIT, false)));
                setHoraris(ctx, horarisNit);
            }
        });
    }

    public static void setHoraris(Context ctx, ArrayList<HorarisNit> horarisNit){
        // Aturem tots els workers d'Horaris que estiguin configurats
        WorkManager.getInstance(ctx)
                .cancelAllWorkByTag(Constants.WORKER_TAG_HORARIS_BLOCK);

        if(!Funcions.accessibilityServiceOn(ctx))
            return;

        AccessibilityScreenService.instance.setHorarisActius(false);

        List<HorarisNit> horaris = new ArrayList<>(horarisNit);

        if(horaris.isEmpty()) {
            AccessibilityScreenService.instance.updateDeviceBlock();
            return;
        }

        int now = DateTime.now().getMillisOfDay();
        int diaSetmana = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        HorarisNit horariAvui = horaris.stream()
                .filter(horarisNit2 -> diaSetmana == horarisNit2.dia)
                .findFirst()
                .orElse(null);

        if(horariAvui == null) {
            AccessibilityScreenService.instance.updateDeviceBlock();
            return;
        }

        int wakeTimeDelay = horariAvui.despertar - now;
        int sleepTimeDelay = horariAvui.dormir - now;

        if(wakeTimeDelay > 0) {
            Funcions.setUpHorariWorker(ctx, wakeTimeDelay, false);
            Funcions.setUpHorariWorker(ctx, sleepTimeDelay, true);

            AccessibilityScreenService.instance.setHorarisActius(true);
        }
        else if(sleepTimeDelay > 0)
            Funcions.setUpHorariWorker(ctx, sleepTimeDelay, true);
        else
            AccessibilityScreenService.instance.setHorarisActius(true);

        AccessibilityScreenService.instance.updateDeviceBlock();
    }

    //POST usage information to server
    public static void sendAppUsage(Context ctx){
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(ctx);
        assert sharedPreferences != null;
        AdicticApi api = ((AdicticApp) ctx.getApplicationContext()).getAPI();

        // Agafem quants dies fa que no s'agafen dades (màxim 6)
        long lastMillisAppUsage = sharedPreferences.getLong(Constants.SHARED_PREFS_LAST_DAY_SENT_DATA, 0);

        // Si hem enviat dades fa menys de 1 minuts, no tornem a enviar
        if(System.currentTimeMillis() - lastMillisAppUsage < 1000*60)
            return;

        int daysToFetch;
        if(lastMillisAppUsage == 0)
            daysToFetch = 6;
        else {
            long lastDay = new DateTime(lastMillisAppUsage).withTimeAtStartOfDay().getMillis();
            long today = new DateTime().withTimeAtStartOfDay().getMillis();
            daysToFetch = Math.min(Math.round(TimeUnit.MILLISECONDS.toDays(Math.abs(today-lastDay))), 6);
        }

        // Agafem les dades
        List<GeneralUsage> gul = Funcions.getGeneralUsages(ctx, daysToFetch);

        long totalTime = gul.stream()
                .mapToLong(generalUsage -> generalUsage.totalTime)
                .sum();

//        long lastTotalUsage = sharedPreferences.getLong(Constants.SHARED_PREFS_LAST_TOTAL_USAGE, 0);

//        // Si és el mateix dia i no ha pujat més de 5 minuts el total, tornem
//        if(sameDay(lastMillisAppUsage, System.currentTimeMillis()) && totalTime - lastTotalUsage < 5 * 60 * 1000)
//            return;

        Call<String> call = api.sendAppUsage(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), gul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if(response.isSuccessful()) {
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LAST_TOTAL_USAGE, totalTime).apply();
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LAST_DAY_SENT_DATA, System.currentTimeMillis()).apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    public static boolean sameDay(long millisDay1, long millisDay2){
        DateTime date1 = new DateTime(millisDay1);
        DateTime date2 = new DateTime(millisDay2);

        return date1.withTimeAtStartOfDay().getMillis() == date2.withTimeAtStartOfDay().getMillis();
    }

    // To check if app has PACKAGE_USAGE_STATS enabled
    public static boolean isAppUsagePermissionOn(Context mContext) {
        boolean granted;
        AppOpsManager appOps = (AppOpsManager) mContext
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), mContext.getPackageName());
        }
        else
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), mContext.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (mContext.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        return granted;
    }

    // To check if accessibility service is enabled
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(mContext.getPackageName()) && enabledServiceInfo.name.equals(AccessibilityScreenService.class.getName()))
                return true;
        }

        String prefString = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if(prefString!= null && prefString.contains(mContext.getPackageName() + "/" + AccessibilityScreenService.class.getName())) {
            Log.e(TAG, "AccessibilityServiceInfo negatiu però prefString positiu");
            return true;
        }
        return false;
    }

    public static boolean isBackgroundLocationPermissionOn(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        else
            return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // To check if Admin Permissions are on
    public static boolean isAdminPermissionsOn(Context mContext) {
        DevicePolicyManager mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<ComponentName> mActiveAdmins = mDPM.getActiveAdmins();

        if (mActiveAdmins == null) return false;

        boolean found = false;
        int i = 0;
        while (!found && i < mActiveAdmins.size()) {
            if (mActiveAdmins.get(i).getPackageName().equals(mContext.getPackageName()))
                found = true;
            i++;
        }
        return found;
    }

    public static int getDayAppUsage(Context mContext, String pkgName){
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar finalTime = Calendar.getInstance();

        Calendar initialTime = Calendar.getInstance();
        initialTime.set(Calendar.HOUR_OF_DAY, 0);
        initialTime.set(Calendar.MINUTE, 0);
        initialTime.set(Calendar.SECOND, 0);

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, initialTime.getTimeInMillis(), finalTime.getTimeInMillis());

        UsageStats appUsageStats = stats.stream()
                .filter(usageStats -> usageStats.getPackageName().equals(pkgName))
                .findFirst()
                .orElse(null);

        if (appUsageStats == null)
            return 0;
        else
            return (int) appUsageStats.getTotalTimeInForeground();
    }

    // **************** WORKERS ****************

    // ForegroundService Worker

    public static void startServiceWorker(Context mCtx){
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(mCtx);
        assert sharedPreferences != null;

        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, true))
            return;

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(ServiceWorker.class, 20, TimeUnit.MINUTES)
                    .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("ServiceWorker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);
    }

    // AppUsageWorkers

    public static void startAppUsageWorker24h(Context mCtx){
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(AppUsageWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                5,
                                TimeUnit.MINUTES)
                        .addTag(Constants.WORKER_TAG_APP_USAGE)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("pujarAppInfo",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);

        Log.d(TAG,"Worker AppUsage 24h Configurat");
    }

    // EventWorkers

    public static void setUpEventWorker(Context mContext, long delay, boolean startEvent){
        Data data = new Data.Builder()
                .putBoolean("start", startEvent)
                .build();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(EventWorker.class, 7, TimeUnit.DAYS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_EVENT_BLOCK)
                        .setInputData(data)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueue(myWork);

        Log.d(TAG,"setUpEventWorker Començat");
    }

    // Horaris Worker

    public static void startHorarisEventsManagerWorker(Context mCtx){
        long startOfDay = DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillisOfDay() + 500;
        long delay = startOfDay - DateTime.now().getMillis();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(HorarisEventsWorkerManager.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                30,
                                TimeUnit.SECONDS
                        )
                        .addTag(Constants.WORKER_TAG_HORARIS_EVENTS_MANAGER)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork(Constants.WORKER_TAG_HORARIS_EVENTS_MANAGER,
                        ExistingPeriodicWorkPolicy.KEEP,
                        myWork);

        Log.d(TAG,"Worker HorarisEventManager Configurat");
    }

    private static void setUpHorariWorker(Context mContext, long delay, boolean startSleep){
        Data data = new Data.Builder()
                .putBoolean("start", startSleep)
                .build();

        WorkRequest myWork;

        myWork = new OneTimeWorkRequest.Builder(HorarisWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(mContext)
                .enqueue(myWork);

        Log.d(TAG,"setUpHorariWorker Començat");
    }

    // GeolocWorkers

    public static void runGeoLocWorker(Context mContext) {
        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(GeoLocWorker.class, 1, TimeUnit.HOURS)
                        .setInitialDelay(0, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_GEOLOC_PERIODIC)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniquePeriodicWork("geoLocWorker", ExistingPeriodicWorkPolicy.KEEP, myWork);
    }

    public static void runGeoLocWorkerOnce(Context mContext) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(GeoLocWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("geoLocWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);
    }

    // **************** END WORKERS ****************



    private static EncryptedFile getEncryptedFile(Context mCtx, String fileName, boolean write){
        File file = new File(mCtx.getFilesDir(),fileName);

        try {
            boolean wasSuccessful = true;
            if(write && file.exists())
                wasSuccessful = file.delete();

            if(!wasSuccessful) {
                String TAG = "Funcions (getEncryptedFile)";
                Log.i(TAG, "No s'ha pogut esborrar el fitxer: " + fileName);
                return null;
            }

            return new EncryptedFile.Builder(
                    mCtx,
                    file,
                    Objects.requireNonNull(getMasterKey(mCtx)),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> void write2File(Context mCtx, String filename, List<T> list){
        // Mirem a quin fitxer escriure
        Objects.requireNonNull(getEncryptedSharedPreferences(mCtx)).edit().putBoolean(filename, true).apply();

        EncryptedFile encryptedFile = getEncryptedFile(mCtx, filename, true);
        if(encryptedFile == null) return;

        if(list != null && !list.isEmpty()){
            Set<T> setList = new HashSet<>(list);

            // Agafem el JSON de la llista i inicialitzem EncryptedFile
            String json = new Gson().toJson(setList);

            // Escrivim al fitxer
            try {
                FileOutputStream fileOutputStream = encryptedFile.openFileOutput();
                fileOutputStream.write(json.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();


            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();

            }
        }
        else{
            File file = new File(mCtx.getFilesDir(), filename);
            if(file.exists()){
                if(file.delete())
                    Log.d(TAG, "write2File: fitxer \"" + filename + "\" s'ha esborrat correctament");
                else
                    Log.d(TAG, "write2File: fitxer \"" + filename + "\" no s'ha pogut esborrar");
            }
        }

    }

    public static <T> List<T> readFromFile(Context mCtx, String filename, boolean storeChanges){
        if(fileEmpty(mCtx,filename)) {
            if(storeChanges)
                Objects.requireNonNull(getEncryptedSharedPreferences(mCtx)).edit().putBoolean(filename, false).apply();
            return new ArrayList<>();
        }

        EncryptedFile encryptedFile = getEncryptedFile(mCtx, filename, false);
        assert encryptedFile != null;

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fileInputStream = encryptedFile.openFileInput();

            InputStreamReader inputStreamReader =
                    new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while(line != null){
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }

            Gson gson = new Gson();
            Type listType = getListType(filename);
            ArrayList<T> res = gson.fromJson(stringBuilder.toString(),listType);
            reader.close();
            inputStreamReader.close();
            fileInputStream.close();

            if(storeChanges)
                Objects.requireNonNull(getEncryptedSharedPreferences(mCtx)).edit().putBoolean(filename, false).apply();

            return res;

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isXiaomi(){
        return Build.MANUFACTURER.equalsIgnoreCase("xiaomi");
    }

    public static boolean isMIUI(){
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            String miui = (String) get.invoke(c, "ro.miui.ui.version.code"); // maybe this one or any other

            // if string miui is not empty, bingo
            return miui != null && !miui.equals("");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileEmpty(Context mCtx, String fileName){
        File file = new File(mCtx.getFilesDir(),fileName);

        // Si el fitxer no existeix el tractem com si fos buit.
        if(!file.exists())
            return true;

        // Retornem si el fitxer està buit
        return file.length() == 0;
    }

    private static Type getListType(String filename) {
        switch (filename) {
            case Constants.FILE_LIMITED_APPS:
                return new TypeToken<ArrayList<BlockedApp>>() {
                }.getType();
            case Constants.FILE_EVENT_BLOCK:
                return new TypeToken<ArrayList<EventBlock>>() {
                }.getType();
            case Constants.FILE_HORARIS_NIT:
                return new TypeToken<ArrayList<HorarisNit>>() {
                }.getType();
            case Constants.FILE_CURRENT_BLOCKED_APPS:
                return new TypeToken<ArrayList<String>>() {
                }.getType();
        }
        return null;
    }

    public static void showBlockAppScreen(Context ctx, String pkgName, String appName) {
        // Si és MIUI
        try {
            if(Funcions.isXiaomi() && false)
                addOverlayView(ctx, false);
            else{
                Log.d(TAG,"Creant Intent cap a BlockAppActivity");
                Intent lockIntent = new Intent(ctx, BlockAppActivity.class);
                lockIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_REORDER_TO_FRONT);
                lockIntent.putExtra("pkgName", pkgName);
                lockIntent.putExtra("appName", appName);
                ctx.startActivity(lockIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchAppBlockFromServer(Context mCtx){
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(mCtx);
        assert sharedPreferences != null;

        if(sharedPreferences.contains(Constants.SHARED_PREFS_IDUSER)) {
            AdicticApi mTodoService = ((AdicticApp) mCtx.getApplicationContext()).getAPI();
            long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Call<BlockedLimitedLists> call = mTodoService.getBlockedLimitedLists(idChild);
            call.enqueue(new Callback<BlockedLimitedLists>() {
                @Override
                public void onResponse(@NonNull Call<BlockedLimitedLists> call, @NonNull Response<BlockedLimitedLists> response) {
                    super.onResponse(call, response);
                    if (response.isSuccessful() && response.body() != null) {
                        updateDB_BlockedApps(mCtx, response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BlockedLimitedLists> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }
    }

    private static void updateDB_BlockedApps(Context ctx, BlockedLimitedLists body) {
        if(!Funcions.accessibilityServiceOn(ctx))
            return;

        List<BlockedApp> appsLimitades = new ArrayList<>();
        List<String> blockedApps = new ArrayList<>();

        if(body.limitApps == null)
            body.limitApps = new ArrayList<>();

        for(LimitedApps limitedApp : body.limitApps){
            if(limitedApp.time > 0) {
                BlockedApp blockedApp = new BlockedApp();
                blockedApp.pkgName = limitedApp.name;
                blockedApp.timeLimit = limitedApp.time;
                appsLimitades.add(blockedApp);
            }
            else
                blockedApps.add(limitedApp.name);
        }

        AccessibilityScreenService.instance.setBlockedApps(blockedApps);
        AccessibilityScreenService.instance.setAppsLimitades(appsLimitades);

//        Funcions.write2File(ctx, Constants.FILE_LIMITED_APPS, appsLimitades);
//        Funcions.write2File(ctx, Constants.FILE_BLOCKED_APPS, blockedApps);

        // Actualitzem mapa Accessibility amb dades noves
        AccessibilityScreenService.instance.setTempsApps(Funcions.getTodayAppUsage(ctx));
        Map<String, Long> map = AccessibilityScreenService.instance.getTempsApps();
        if(map == null)
            map = new HashMap<>();
        for(BlockedApp limitedApp : appsLimitades) {
            if(limitedApp.pkgName == null)
                continue;
            long tempsUsat = map.getOrDefault(limitedApp.pkgName, 0L);
            if (tempsUsat > limitedApp.timeLimit)
                AccessibilityScreenService.instance.addBlockedApp(limitedApp.pkgName);
        }

        AccessibilityScreenService.instance.isCurrentAppBlocked();
    }

    public static boolean accessibilityServiceOn(Context mCtx){
        boolean res = AccessibilityScreenService.instance != null;

        if(!res){
            NotificationInformation notif = new NotificationInformation();
            notif.title = mCtx.getString(R.string.notif_accessibility_error_title);
            notif.message = mCtx.getString(R.string.notif_accessibility_error_body);
            notif.important = true;
            notif.dateMillis = System.currentTimeMillis();
            notif.read = false;
            notif.notifCode = Constants.NOTIF_SETTINGS_ACCESSIBILITY_ERROR;

            sendNotifToParent(mCtx, notif);
        }

        return res;
    }

    public static void sendNotifToParent(Context mCtx, NotificationInformation notif) {
        AdicticApi api = ((AdicticApp) mCtx.getApplicationContext()).getAPI();

        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(mCtx);
        assert sharedPreferences != null;

        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);

        Call<String> call = api.sendNotification(idChild, notif);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                startNotificationWorker(mCtx, notif, idChild);
            }
        });
    }

    private static void startNotificationWorker(Context mCtx, NotificationInformation notif, Long idChild){
        Data data = new Data.Builder()
                .putString("title", notif.title)
                .putString("body", notif.message)
                .putBoolean("important", notif.important)
                .putLong("dateMillis", notif.dateMillis)
                .putLong("idChild", idChild)
                .putString("notifCode", notif.notifCode)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest myWork =
                new OneTimeWorkRequest.Builder(NotifWorker.class)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                5,
                                TimeUnit.MINUTES)
                        .setInputData(data)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueue(myWork);
    }

    public static Map<String, Long> getTodayAppUsage(Context mContext) {
        long startOfDay = new DateTime().withTimeAtStartOfDay().getMillis();
        long now = System.currentTimeMillis();

        List<AppUsage> listUsages = getAppUsages(mContext, startOfDay, now).first;

        return listUsages.stream()
                .collect(Collectors.toMap(appUsage -> appUsage.app.pkgName,appUsage -> appUsage.totalTime));
    }

    public static void addNotificationToList(Context context, NotificationInformation notificationInformation) {
        final int MAX_NOTIF_SIZE = 25;

        SharedPreferences sharedPreferences = com.adictic.common.util.Funcions.getEncryptedSharedPreferences(context);
        assert sharedPreferences != null;

        String json = sharedPreferences.getString(Constants.SHARED_PREFS_NOTIFS, "");
        Type type = new TypeToken<ArrayList<NotificationInformation>>() {}.getType();

        Gson gson = new Gson();
        ArrayList<NotificationInformation> notifList = gson.fromJson(json, type) != null ? gson.fromJson(json, type) : new ArrayList<>();

        // Si la llista té 15 elements
        if(notifList.size() == MAX_NOTIF_SIZE){
            NotificationInformation oldNotif = notifList.stream()
                    .min(Comparator.comparing(v -> v.dateMillis)).get();

            notifList.remove(oldNotif);
        }
        else if(notifList.size() > MAX_NOTIF_SIZE){
            for(int i = 0; i < notifList.size() - (MAX_NOTIF_SIZE-1); i++){
                NotificationInformation oldNotif = notifList.stream()
                        .min(Comparator.comparing(v -> v.dateMillis)).get();

                notifList.remove(oldNotif);
            }
        }

        notifList.add(notificationInformation);

        String newJson = gson.toJson(notifList);
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_NOTIFS, newJson).apply();
    }

    public static ArrayList<NotificationInformation> getNotificationList(Context context){
        SharedPreferences sharedPreferences = com.adictic.common.util.Funcions.getEncryptedSharedPreferences(context);
        assert sharedPreferences != null;

        String json = sharedPreferences.getString(Constants.SHARED_PREFS_NOTIFS, "");
        Type type = new TypeToken<ArrayList<NotificationInformation>>() {}.getType();

        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public static void setNotificationList(Context context, List<NotificationInformation> list){
        SharedPreferences sharedPreferences = com.adictic.common.util.Funcions.getEncryptedSharedPreferences(context);
        assert sharedPreferences != null;

        Gson gson = new Gson();

        String newJson = gson.toJson(list);
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_NOTIFS, newJson).apply();
    }
}
