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
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.ui.BlockAppActivity;
import com.adictic.client.workers.AppUsageWorker;
import com.adictic.client.workers.EventWorker;
import com.adictic.client.workers.GeoLocWorker;
import com.adictic.client.workers.HorarisEventsWorkerManager;
import com.adictic.client.workers.HorarisWorker;
import com.adictic.client.workers.NotifWorker;
import com.adictic.client.workers.ServiceWorker;
import com.adictic.common.callbacks.BooleanCallback;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.EventsAPI;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.entity.LimitedApps;
import com.adictic.common.entity.NotificationInformation;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.rest.Api;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.hilt.Repository;

import org.joda.time.DateTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class Funcions extends com.adictic.common.util.Funcions {
    private final static String TAG = "Funcions";

    @Inject
    static Repository repository;

//    public static void addOverlayView(Context ctx, boolean blockApp) {
//
//        final WindowManager.LayoutParams params;
//        int layoutParamsType;
//
//        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }
//        else {
//            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//
//        params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                layoutParamsType,
//                0,
//                PixelFormat.TRANSLUCENT);
//
//        params.gravity = Gravity.CENTER | Gravity.START;
//        params.x = 0;
//        params.y = 0;
//
//        FrameLayout interceptorLayout = new FrameLayout(ctx) {
//
//            @Override
//            public boolean dispatchKeyEvent(KeyEvent event) {
//
//                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//
//                    // Check if the HOME button is pressed
//                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//
//                        Log.v(TAG, "BACK Button Pressed");
//
//                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
//                        return true;
//                    }
//                }
//
//                // Otherwise don't intercept the event
//                return super.dispatchKeyEvent(event);
//            }
//        };
//
//        LayoutInflater inflater = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
//
//        if (inflater != null) {
//            if (!blockApp){
//                View floatyView = inflater.inflate(R.layout.block_layout, interceptorLayout);
//                windowManager.addView(floatyView, params);
//
//                Button BT_sortir = floatyView.findViewById(R.id.btn_sortir);
//                BT_sortir.setOnClickListener(view -> {
//                    Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
//                    startHomescreen.addCategory(Intent.CATEGORY_HOME);
//                    startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    ctx.startActivity(startHomescreen);
//                    windowManager.removeView(floatyView);
//                });
//            }
//            else{
//                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);
//                assert sharedPreferences != null;
//
//                boolean blockedDevice = sharedPreferences.getBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE, false);
//
//                View floatyView = inflater.inflate(R.layout.block_device_layout, interceptorLayout);
//                windowManager.addView(floatyView, params);
//
//                TextView blockDeviceTitle = floatyView.findViewById(R.id.TV_block_device_title);
//                blockDeviceTitle.setText(ctx.getString(R.string.locked_device));
//
//                if (!blockedDevice){
//                    List<EventBlock> eventsList = repository.getEventsByDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
//
//                    EventBlock eventBlock = eventsList.stream()
//                            .filter(com.adictic.common.util.Funcions::eventBlockIsActive)
//                            .findFirst()
//                            .orElse(null);
//
//                    if(eventBlock != null){
//                        String title = eventBlock.name + "\n";
//                        title += millis2horaString(ctx, eventBlock.startEvent) + " - " + millis2horaString(ctx, eventBlock.endEvent);
//                        blockDeviceTitle.setText(title);
//                    }
//                }
//
//                ConstraintLayout CL_device_blocked_call = floatyView.findViewById(R.id.CL_block_device_emergency_call);
//                CL_device_blocked_call.setOnClickListener(view -> {
//                    Uri number = Uri.parse("tel:" + 112);
//                    Intent dial = new Intent(Intent.ACTION_CALL, number);
//                    ctx.startActivity(dial);
//                });
//            }
//        }
//        else {
//            Log.e("SAW-example", "Layout Inflater Service is null; can't inflate and display R.layout.floating_view");
//        }
//    }

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

//    public static void showBlockAppScreen(Context ctx, String pkgName, String appName) {
//        // Si és MIUI
//        try {
//            if(Funcions.isXiaomi() && false)
//                addOverlayView(ctx, false);
//            else{
//                Log.d(TAG,"Creant Intent cap a BlockAppActivity");
//                Intent lockIntent = new Intent(ctx, BlockAppActivity.class);
//                lockIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_REORDER_TO_FRONT);
//                lockIntent.putExtra("pkgName", pkgName);
//                lockIntent.putExtra("appName", appName);
//                ctx.startActivity(lockIntent);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }




}
