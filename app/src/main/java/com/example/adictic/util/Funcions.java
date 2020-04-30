package com.example.adictic.util;

import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.service.LimitAppsWorker;
import com.example.adictic.service.WindowChangeDetectingService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Funcions {

    // To check if app has PACKAGE_USAGE_STATS enabled
    public static boolean isAppUsagePermissionOn(Context mContext){
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) mContext
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
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
        String TAG = "Accessibility Settings";

        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + WindowChangeDetectingService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            return true;
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
            return false;
        }
    }

    // To check if Admin Permissions are on
    public static boolean isAdminPermissionsOn(Context mContext){
        DevicePolicyManager mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<ComponentName> mActiveAdmins = mDPM.getActiveAdmins();

        if(mActiveAdmins == null) return false;

        Boolean found = false;
        int i = 0;
        while(!found && i < mActiveAdmins.size()){
            if(mActiveAdmins.get(i).getPackageName().equals(mContext.getPackageName())) found = true;
            i++;
        }
        return found;
    }

    public static void runLimitAppsWorker(Context mContext, long delay){
        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(LimitAppsWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("checkLimitedApps", ExistingWorkPolicy.REPLACE, myWork);
    }

    /** pre: si fTime = -1, agafa valors del dia actual inacabat **/
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

            gu.totalTime = Long.parseLong("0");
            for(AppUsage au : appUsages){
                gu.totalTime += au.totalTime;
            }

            gul.add(gu);
        } else {
            for (int i = iTime; i < fTime; i++) {
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

                gul.add(gu);
            }
        }
        return gul;
    }

    private static List<AppUsage> getAppUsages(Context mContext, Calendar initialTime, Calendar finalTime){
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
                e.printStackTrace();
            }
            if (pkgStats.getLastTimeUsed() >= initialTime.getTimeInMillis() && pkgStats.getLastTimeUsed() <= finalTime.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppUsage appUsage = new AppUsage();
                appUsage.appName = pkgStats.getPackageName();
                appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                appUsages.add(appUsage);
            }
        }

        return appUsages;
    }

    public static void updateLimitedAppsList(){
        long millisToAdd = Calendar.getInstance().getTimeInMillis() - TodoApp.getStartFreeUse();
        Map<String,Long> newMap = new HashMap<>();
        for(Map.Entry<String,Long> entry : TodoApp.getLimitApps().entrySet()){
            newMap.put(entry.getKey(),entry.getValue()+millisToAdd);
        }

        TodoApp.setLimitApps(newMap);
    }

    public static void startFreeUseLimitList(Context mContext){
        List<GeneralUsage> gul = getGeneralUsages(mContext,0,-1);
        GeneralUsage gu = gul.get(0);

        List<AppUsage> appUsages = (List<AppUsage>)gu.usage;

        Map<String,Long> newMap = new HashMap<>();

        for(Map.Entry<String,Long> entry : TodoApp.getLimitApps().entrySet()){
            AppUsage appUsage = appUsages.get(appUsages.indexOf(entry.getKey()));

            newMap.put(entry.getKey(),entry.getValue()-appUsage.totalTime);
        }

        TodoApp.setLimitApps(newMap);
    }
}
