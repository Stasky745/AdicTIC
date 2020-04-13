package com.example.adictic.util;

import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.example.adictic.service.WindowChangeDetectingService;

import java.util.List;

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
        }
        return found;
    }
}
