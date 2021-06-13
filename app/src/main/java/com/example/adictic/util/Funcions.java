package com.example.adictic.util;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.adictic.BuildConfig;
import com.example.adictic.R;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.BlockedApp;
import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.EventBlock;
import com.example.adictic.entity.EventsAPI;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.HorarisAPI;
import com.example.adictic.entity.HorarisNit;
import com.example.adictic.entity.LimitedApps;
import com.example.adictic.entity.MonthEntity;
import com.example.adictic.entity.YearEntity;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.service.WindowChangeDetectingService;
import com.example.adictic.workers.AppUsageWorker;
import com.example.adictic.workers.GeoLocWorker;
import com.example.adictic.workers.UpdateTokenWorker;
import com.example.adictic.workers.block_apps.BlockAppWorker;
import com.example.adictic.workers.block_apps.RestartBlockedApps;
import com.example.adictic.workers.event_workers.FinishBlockEventWorker;
import com.example.adictic.workers.event_workers.RestartEventsWorker;
import com.example.adictic.workers.event_workers.StartBlockEventWorker;
import com.example.adictic.workers.horaris_workers.DespertarWorker;
import com.example.adictic.workers.horaris_workers.DormirWorker;
import com.example.adictic.workers.horaris_workers.RestartHorarisWorker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.adictic.util.Constants.KEY_SIZE;
import static com.example.adictic.util.Constants.SHARED_PREFS_CHANGE_BLOCKED_APPS;
import static com.example.adictic.util.Constants.SHARED_PREFS_CHANGE_EVENT_BLOCK;
import static com.example.adictic.util.Constants.SHARED_PREFS_CHANGE_HORARIS_NIT;

public class Funcions {
    private final static String TAG = "Funcions";

    public static String formatHora(int hora, int min){
        String res = "";

        if(hora < 10)
            res += "0"+hora;
        else
            res += hora;

        res += ":";

        if(min < 10)
            res += "0"+min;
        else
            res += min;

        return res;
    }

    public static String date2String(int dia, int mes, int any) {
        String data;
        if (dia < 10) data = "0" + dia + "-";
        else data = dia + "-";
        if (mes < 10) data += "0" + mes + "-";
        else data += mes + "-";

        return data + any;
    }

    public static void setIconDrawable(Context ctx, String pkgName, final ImageView d) {

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Uri imageUri = Uri.parse(URL).buildUpon()
                .appendPath("icons")
                .appendPath(pkgName)
                .build();

        Glide.with(ctx)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(d);
    }

    public static void checkEvents(Context ctx) {
        Log.d(TAG,"Check Events");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);

        TodoApi mTodoService = ((TodoApp) (ctx.getApplicationContext())).getAPI();

        assert sharedPreferences != null;

        // Agafem els horaris de la nit i Events
        Call<EventsAPI> call = mTodoService.getEvents(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<EventsAPI>() {
            @Override
            public void onResponse(@NonNull Call<EventsAPI> call, @NonNull Response<EventsAPI> response) {
                if (response.isSuccessful()) {
                    if(response.body() == null)
                        write2File(ctx, Constants.FILE_EVENT_BLOCK, null);
                    else
                        write2File(ctx, Constants.FILE_EVENT_BLOCK, response.body().events);

                    // Engeguem els workers
                    runRestartEventsWorkerOnce(ctx,0);
                    startRestartEventsWorker24h(ctx);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventsAPI> call, @NonNull Throwable t) { }
        });
    }

    public static void checkHoraris(Context ctx) {
        Log.d(TAG,"Check Horaris");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);

        TodoApi mTodoService = ((TodoApp) (ctx.getApplicationContext())).getAPI();

        assert sharedPreferences != null;

        // Agafem els horaris de la nit i Events
        Call<HorarisAPI> call = mTodoService.getHoraris(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<HorarisAPI>() {
            @Override
            public void onResponse(@NonNull Call<HorarisAPI> call, @NonNull Response<HorarisAPI> response) {
                if (response.isSuccessful()) {
                    if(response.body() == null || response.body().horarisNit.isEmpty())
                        write2File(ctx, Constants.FILE_HORARIS_NIT, null);
                    else
                        write2File(ctx, Constants.FILE_HORARIS_NIT, response.body().horarisNit);

                    // Engeguem els workers
                    runRestartHorarisWorkerOnce(ctx,0);
                    startRestartHorarisWorker24h(ctx);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HorarisAPI> call, @NonNull Throwable t) { }
        });
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
            if (enabledServiceInfo.packageName.equals(mContext.getPackageName()) && enabledServiceInfo.name.equals(WindowChangeDetectingService.class.getName()))
                return true;
        }

        String prefString = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if(prefString!= null && prefString.contains(mContext.getPackageName() + "/" + WindowChangeDetectingService.class.getName())) {
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

    public static Pair<Integer, Integer> millisToString(float l) {
        float minuts = l / (60000);
        int hores = 0;

        while (minuts >= 60) {
            hores++;
            minuts -= 60;
        }

        return new Pair<>(hores, Math.round(minuts));
    }

    // retorna -1 si no hi ha hora establerta
    public static int string2MillisOfDay(String time){
        if(time == null || time.equals(""))
            return -1;

        String[] time2 = time.split(":");
        DateTime dateTime = new DateTime()
                .withHourOfDay(Integer.parseInt(time2[0]))
                .withMinuteOfHour(Integer.parseInt(time2[1]));

        return dateTime.getMillisOfDay();
    }

    public static String millisOfDay2String(int millis){
        DateTime dateTime = new DateTime()
                .withMillisOfDay(millis);

        return formatHora(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
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

    public static Map<Integer, Map<Integer, List<Integer>>> convertYearEntityToMap(List<YearEntity> yearList) {
        Map<Integer, Map<Integer, List<Integer>>> res = new HashMap<>();
        for (YearEntity yEntity : yearList) {
            Map<Integer, List<Integer>> mMap = new HashMap<>();
            for (MonthEntity mEntity : yEntity.months) {
                mMap.put(mEntity.month, mEntity.days);
            }
            res.put(yEntity.year, mMap);
        }

        return res;
    }

    public static void updateDB_BlockedApps(Context ctx, BlockedLimitedLists body) {
        List<BlockedApp> llista = new ArrayList<>();

        for(LimitedApps limitedApp : body.limitApps){
            BlockedApp blockedApp = new BlockedApp();
            blockedApp.pkgName = limitedApp.name;
            blockedApp.timeLimit = limitedApp.time;
            llista.add(blockedApp);
        }

        write2File(ctx, Constants.FILE_BLOCKED_APPS,llista);

        runRestartBlockedAppsWorkerOnce(ctx,0);
        startRestartBlockedAppsWorker24h(ctx);
    }

    // **************** WORKERS ****************

    // AppUsageWorkers

    public static void startAppUsageWorker24h(Context mCtx){
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(mCtx);
        Calendar cal = Calendar.getInstance();
        // Agafem dades dels últims X dies per inicialitzar dades al servidor
        cal.add(Calendar.DAY_OF_YEAR, -6);
        assert sharedPreferences != null;
        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_DAYOFYEAR,cal.get(Calendar.DAY_OF_YEAR)).apply();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(AppUsageWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("pujarAppInfo",
                        ExistingPeriodicWorkPolicy.KEEP,
                        myWork);

        Log.d(TAG,"Worker AppUsage 24h Configurat");

        runUniqueAppUsageWorker(mCtx);
    }

    public static void runUniqueAppUsageWorker(Context mContext) {
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(AppUsageWorker.class)
                        .setInitialDelay(0, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("uniqueAppUsageWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker AppUsage (únic) Començat");
    }

    // EventWorkers

    public static void startRestartEventsWorker24h(Context mCtx){
        long now = DateTime.now().getMillisOfDay();
        long delay = Constants.TOTAL_MILLIS_IN_DAY + 30000 - now; // 30 segons més de mitjanit

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(RestartEventsWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("24h_eventBlock",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);

        Log.d(TAG,"Worker RestartEvents 24h Configurat");
    }

    public static void runRestartEventsWorkerOnce(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(RestartEventsWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("EventsWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker RestartEvents (únic) Començat");
    }

    public static void runStartBlockEventWorker(Context mContext, long id, long delay) {
        Data.Builder data = new Data.Builder();
        data.putLong("id", id);

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(StartBlockEventWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .addTag(Constants.WORKER_TAG_EVENT_BLOCK)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork(String.valueOf(id), ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker StartBlockEvent Configurat - ID=" + id + " | delay=" + delay);
    }

    public static void runFinishBlockEventWorker(Context mContext, long id, long delay) {
        Data.Builder data = new Data.Builder();
        data.putLong("id", id);

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(FinishBlockEventWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .addTag(Constants.WORKER_TAG_EVENT_BLOCK)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork(String.valueOf(id), ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker FinishBlockEvent Configurat - ID=" + id + " | delay=" + delay);
    }

    // Horaris Worker

    public static void startRestartHorarisWorker24h(Context mCtx){
        long now = DateTime.now().getMillisOfDay();
        long delay = Constants.TOTAL_MILLIS_IN_DAY + 30000 - now; // 30 segons més de mitjanit

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(RestartHorarisWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("24h_horarisWorker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);

        Log.d(TAG,"Worker RestartEvents 24h Configurat");
    }

    public static void runRestartHorarisWorkerOnce(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(RestartHorarisWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("HorarisWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker RestartEvents (únic) Començat");
    }

    public static void runDespertarWorker(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(DespertarWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_HORARIS_BLOCK)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("despertarWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker Despertar Configurat - delay=" + delay);
    }

    public static void runDormirWorker(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(DormirWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_HORARIS_BLOCK)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("dormirWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker Dormir Configurat - delay=" + delay);
    }

    // BlockAppsWorkers

    public static void startRestartBlockedAppsWorker24h(Context mCtx){
        long now = DateTime.now().getMillisOfDay();
        long delay = Constants.TOTAL_MILLIS_IN_DAY + 30000 - now; // 30 segons més de mitjanit

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(RestartBlockedApps.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mCtx)
                .enqueueUniquePeriodicWork("24h_blockApps",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);

        Log.d(TAG,"Worker RestartBlockedApps 24h Configurat");
    }

    public static void runRestartBlockedAppsWorkerOnce(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(RestartBlockedApps.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("BlockedAppsWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker RestartBlockedApps (únic) Començat");
    }

    public static void runBlockAppsWorker(Context mContext, long delay) {
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(BlockAppWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_BLOCK_APPS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("blockAppWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker BlockApp Configurat - Delay=" + delay);
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
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(GeoLocWorker.class)
                        .setInitialDelay(0, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("geoLocWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);
    }

    // UpdateTokenWorker

    public static void runUpdateTokenWorker(Context mContext, long idUser, String token, long delay){
        Data.Builder data = new Data.Builder();
        data.putLong("idUser", idUser);
        data.putString("token", token);

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(UpdateTokenWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("UpdateTokenWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker UpdateToken Configurat - ID=" + idUser + " | delay=" + delay);
    }

    // **************** END WORKERS ****************

    /**
     * pre: si fTime = -1, agafa valors del dia actual inacabat
     * post: retorna la llista amb els mesos ja adaptats pel servidor (+1)
     **/
    public static List<GeneralUsage> getGeneralUsages(Context mContext, int iTime, int fTime) {
        List<GeneralUsage> gul = new ArrayList<>();

        if (fTime == -1) {
            Calendar finalTime = Calendar.getInstance();

            Calendar initialTime = Calendar.getInstance();
            initialTime.set(Calendar.HOUR_OF_DAY, 0);
            initialTime.set(Calendar.MINUTE, 0);
            initialTime.set(Calendar.SECOND, 0);

            List<AppUsage> appUsages = getAppUsages(mContext, initialTime, finalTime);

            GeneralUsage gu = new GeneralUsage();
            gu.day = finalTime.get(Calendar.DAY_OF_MONTH);
            gu.month = finalTime.get(Calendar.MONTH) + 1;
            gu.year = finalTime.get(Calendar.YEAR);
            gu.usage = appUsages;

            gu.totalTime = 0L;
            for (AppUsage au : appUsages) {
                gu.totalTime += au.totalTime;
            }

            gul.add(gu);
        } else {
            for (int i = iTime; i <= fTime; i++) {
                Calendar finalTime = Calendar.getInstance();
                finalTime.set(Calendar.DAY_OF_YEAR, i);
                finalTime.set(Calendar.HOUR_OF_DAY, 23);
                finalTime.set(Calendar.MINUTE, 59);
                finalTime.set(Calendar.SECOND, 59);

                Calendar initialTime = Calendar.getInstance();
                initialTime.set(Calendar.DAY_OF_YEAR, i);
                initialTime.set(Calendar.HOUR_OF_DAY, 0);
                initialTime.set(Calendar.MINUTE, 0);
                initialTime.set(Calendar.SECOND, 0);

                List<AppUsage> appUsages = getAppUsages(mContext, initialTime, finalTime);

                GeneralUsage gu = new GeneralUsage();
                gu.day = finalTime.get(Calendar.DAY_OF_MONTH);
                gu.month = finalTime.get(Calendar.MONTH) + 1;
                gu.year = finalTime.get(Calendar.YEAR);
                gu.usage = appUsages;

                gu.totalTime = 0L;
                for (AppUsage au : appUsages) {
                    gu.totalTime += au.totalTime;
                }

                gul.add(gu);
            }
        }
        return gul;
    }

    private static List<AppUsage> getAppUsages(Context mContext, Calendar initialTime, Calendar finalTime) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager mPm = mContext.getPackageManager();

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                initialTime.getTimeInMillis(), finalTime.getTimeInMillis());

        List<AppUsage> appUsages = new ArrayList<>();
        final int statCount = stats.size();
        for (int j = 0; j < statCount; j++) {
            final android.app.usage.UsageStats pkgStats = stats.get(j);
            ApplicationInfo appInfo = null;
            try {
                appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }
            if (appInfo != null && pkgStats.getLastTimeUsed() >= initialTime.getTimeInMillis() && pkgStats.getLastTimeUsed() <= finalTime.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppUsage appUsage = new AppUsage();
                appUsage.app = new AppInfo();

                if (Build.VERSION.SDK_INT >= 26) appUsage.app.category = appInfo.category;
                appUsage.app.appName = mPm.getApplicationLabel(appInfo).toString();
                appUsage.app.pkgName = pkgStats.getPackageName();
                appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                appUsages.add(appUsage);
            }
        }

        return appUsages;
    }

    /**
     * Retorna -1 als dos valors si no és un string acceptable
     **/
    public static Pair<Integer, Integer> stringToTime(String s) {
        int hour, minutes;
        String[] hora = s.split(":");

        if (hora.length != 2) {
            hour = -1;
            minutes = -1;
        } else {
            if (Integer.parseInt(hora[0]) < 0 || Integer.parseInt(hora[0]) > 23) {
                hour = -1;
                minutes = -1;
            } else if (Integer.parseInt(hora[1]) < 0 || Integer.parseInt(hora[1]) > 59) {
                hour = -1;
                minutes = -1;
            } else {
                hour = Integer.parseInt(hora[0]);
                minutes = Integer.parseInt(hora[1]);
            }
        }

        return new Pair<>(hour, minutes);
    }

    public static void canviarMesosDeServidor(Collection<GeneralUsage> generalUsages) {
        for (GeneralUsage generalUsage : generalUsages) {
            generalUsage.month -= 1;
        }
    }

    public static void canviarMesosAServidor(Collection<GeneralUsage> generalUsages) {
        for (GeneralUsage generalUsage : generalUsages) {
            generalUsage.month += 1;
        }
    }

    public static void canviarMesosDeServidor(List<YearEntity> yearList) {
        for (YearEntity yearEntity : yearList) {
            for (MonthEntity monthEntity : yearEntity.months) {
                monthEntity.month -= 1;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void closeKeyboard(View view, Activity a) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(a);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                closeKeyboard(innerView, a);
            }
        }
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText() && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    public static void askChildForLiveApp(Context ctx, long idChild, boolean liveApp) {
        TodoApi mTodoService = ((TodoApp) (ctx.getApplicationContext())).getAPI();
        Call<String> call = mTodoService.askChildForLiveApp(idChild, liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (!response.isSuccessful()) {
                    Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public static String getFullURL(String url) {
        if (!url.contains("https://")) return "https://" + url;
        else return url;
    }

    /**
     * SHARED PREFERENCES
     */

    private static MasterKey getMasterKey(Context mCtx) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    Constants.MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .build();

            return new MasterKey.Builder(mCtx)
                    .setKeyGenParameterSpec(spec)
                    .build();
        } catch (Exception e) {
            Log.e(mCtx.getClass().getSimpleName(), "Error on getting master key", e);
        }
        return null;
    }

    public static SharedPreferences getEncryptedSharedPreferences(Context mCtx) {
        try {
            if(TodoApp.getSharedPreferences()==null) {
                TodoApp.setSharedPreferences(EncryptedSharedPreferences.create(
                        mCtx,
                        "values",
                        Objects.requireNonNull(getMasterKey(mCtx)), // calling the method above for creating MasterKey
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ));
            }
            return TodoApp.getSharedPreferences();
        } catch (Exception e) {
            Log.e(mCtx.getClass().getSimpleName(), "Error on getting encrypted shared preferences", e);
        }
        return null;
    }

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
        return Build.MANUFACTURER.toLowerCase().equals("xiaomi");
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
            case Constants.FILE_BLOCKED_APPS:
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

    public static void endFreeUse(Context mCtx) {
        SharedPreferences sharedPreferences = getEncryptedSharedPreferences(mCtx);
        assert sharedPreferences != null;
        boolean isBlocked = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE, false) ||
                sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, 0) > 0 ||
                sharedPreferences.getBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT, false);
        if(isBlocked) {
            DevicePolicyManager mDPM = (DevicePolicyManager) mCtx.getSystemService(Context.DEVICE_POLICY_SERVICE);
            assert mDPM != null;
            mDPM.lockNow();
        }
    }
}
