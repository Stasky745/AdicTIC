package com.example.adictic.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Pair;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.MonthEntity;
import com.example.adictic.entity.TimeDay;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.entity.YearEntity;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.service.LimitAppsWorker;
import com.example.adictic.service.WindowChangeDetectingService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Funcions {
    
    private static void setHoraris(WakeSleepLists list){
        TimeDay sleep = list.sleep;
        TimeDay wake = list.wake;
        
        Map<Integer,String> sleepMap = new HashMap<>();
        Map<Integer,String> wakeMap = new HashMap<>();
        
        sleepMap.put(Calendar.MONDAY,sleep.monday);
        sleepMap.put(Calendar.TUESDAY,sleep.tuesday);
        sleepMap.put(Calendar.WEDNESDAY,sleep.wednesday);
        sleepMap.put(Calendar.THURSDAY,sleep.thursday);
        sleepMap.put(Calendar.FRIDAY,sleep.friday);
        sleepMap.put(Calendar.SATURDAY,sleep.saturday);
        sleepMap.put(Calendar.SUNDAY,sleep.sunday);

        wakeMap.put(Calendar.MONDAY,wake.monday);
        wakeMap.put(Calendar.TUESDAY,wake.tuesday);
        wakeMap.put(Calendar.WEDNESDAY,wake.wednesday);
        wakeMap.put(Calendar.THURSDAY,wake.thursday);
        wakeMap.put(Calendar.FRIDAY,wake.friday);
        wakeMap.put(Calendar.SATURDAY,wake.saturday);
        wakeMap.put(Calendar.SUNDAY,wake.sunday);

        TodoApp.setWakeHoraris(wakeMap);
        TodoApp.setSleepHoraris(sleepMap);
    }

    private static long getHorariInMillis(){
        Calendar cal = Calendar.getInstance();
        String wakeTime = TodoApp.getWakeHoraris().get(cal.get(Calendar.DAY_OF_WEEK));
        String sleepTime = TodoApp.getSleepHoraris().get(cal.get(Calendar.DAY_OF_WEEK));

        int wakeHour = Integer.parseInt(wakeTime.split(":")[0]);
        int wakeMinute = Integer.parseInt(wakeTime.split(":")[1]);

        int sleepHour = Integer.parseInt(sleepTime.split(":")[0]);
        int sleepMinute = Integer.parseInt(sleepTime.split(":")[1]);

        Calendar calWake = Calendar.getInstance();
        calWake.set(Calendar.HOUR_OF_DAY,wakeHour);
        calWake.set(Calendar.MINUTE,wakeMinute);

        Calendar calSleep = Calendar.getInstance();
        calSleep.set(Calendar.HOUR_OF_DAY,sleepHour);
        calSleep.set(Calendar.MINUTE,sleepMinute);

        long timeNow = cal.getTimeInMillis();
        long wakeMillis = calWake.getTimeInMillis();
        long sleepMillis = calSleep.getTimeInMillis();

        if(wakeMillis > sleepMillis){
            if(timeNow>=wakeMillis){
                TodoApp.setBlockedDevice(false);
                calSleep.add(Calendar.DAY_OF_YEAR,1);

                return calSleep.getTimeInMillis();
            }
            else if(timeNow>=sleepMillis) {
                TodoApp.setBlockedDevice(true);
                return wakeMillis;
            }
            else{
                return sleepMillis;
            }
        }
        else{
            if(timeNow>=sleepMillis){
                TodoApp.setBlockedDevice(true);
                calWake.add(Calendar.DAY_OF_YEAR,1);

                return calWake.getTimeInMillis();
            }
            else if(timeNow>=wakeMillis){
                TodoApp.setBlockedDevice(false);
                return sleepMillis;
            }
            else return wakeMillis;
        }
    }

    public static void setIconDrawable(Context ctx, String pkgName, final ImageView d){
        TodoApi mTodoService = ((TodoApp)(ctx.getApplicationContext())).getAPI();

        Call<ResponseBody> call = mTodoService.getIcon(pkgName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                    d.setImageBitmap(bmp);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public static long checkHoraris(Context ctx){
        
        TodoApi mTodoService = ((TodoApp)(ctx.getApplicationContext())).getAPI();
        
        Call<WakeSleepLists> call = mTodoService.getHoraris(TodoApp.getIDChild());

        final long[] res = {-1};

        call.enqueue(new Callback<WakeSleepLists>() {
            @Override
            public void onResponse(Call<WakeSleepLists> call, Response<WakeSleepLists> response) {
                if(response.isSuccessful() && response.body() != null){
                    setHoraris(response.body());
                    res[0] = getHorariInMillis();
                }
                else if(!TodoApp.getSleepHoraris().isEmpty() && !TodoApp.getWakeHoraris().isEmpty()) res[0] = getHorariInMillis();
                else res[0] = -2;
            }

            @Override
            public void onFailure(Call<WakeSleepLists> call, Throwable t) {
                if(!TodoApp.getSleepHoraris().isEmpty() && !TodoApp.getWakeHoraris().isEmpty()) res[0] = getHorariInMillis();
                else res[0] = -2;
            }
        });

        while(res[0] == -1){}

        return res[0];
    }

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
        AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(mContext.getPackageName()) && enabledServiceInfo.name.equals(WindowChangeDetectingService.class.getName()))
                return true;
        }

        return false;
    }

    public static Pair<Integer,Integer> millisToString(float l){
        float minuts = l/(60000);
        int hores = 0;

        while(minuts >= 60){
            hores++;
            minuts-=60;
        }

        Pair<Integer,Integer> res = new Pair<>(hores,Math.round(minuts));
        return res;
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

    public static Map<Integer,Map<Integer,List<Integer>>> convertYearEntityToMap(List<YearEntity> yearList){
        Map<Integer,Map<Integer,List<Integer>>> res = new HashMap<>();
        for(YearEntity yEntity : yearList){
            Map<Integer,List<Integer>> mMap = new HashMap<>();
            for(MonthEntity mEntity : yEntity.months){
                mMap.put(mEntity.month,mEntity.days);
            }
            res.put(yEntity.year,mMap);
        }

        return res;
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
                //e.printStackTrace();
            }
            if (appInfo!=null && pkgStats.getLastTimeUsed() >= initialTime.getTimeInMillis() && pkgStats.getLastTimeUsed() <= finalTime.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppUsage appUsage = new AppUsage();
                AppInfo app = new AppInfo();
                appUsage.app = app;

                if(Build.VERSION.SDK_INT >= 26) appUsage.app.category = appInfo.category;
                appUsage.app.appName = mPm.getApplicationLabel(appInfo).toString();
                appUsage.app.pkgName = pkgStats.getPackageName();
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

        TodoApp.setStartFreeUse(0);
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

        TodoApp.setStartFreeUse(Calendar.getInstance().getTimeInMillis());
        TodoApp.setLimitApps(newMap);
    }
}
