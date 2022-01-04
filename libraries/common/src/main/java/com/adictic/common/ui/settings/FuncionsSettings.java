package com.adictic.common.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.adictic.common.R;
import com.adictic.common.ui.NotificationSettings;
import com.adictic.common.ui.settings.notifications.NotificationActivity;
import com.adictic.common.util.BiometricAuthUtil;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;

import java.util.Objects;

public class FuncionsSettings {

    public static void settings_android(PreferenceFragmentCompat context) {
        Preference setting_android = context.findPreference("setting_android");
        ApplicationInfo appInfo = context.requireContext().getApplicationContext().getApplicationInfo();

        assert setting_android != null;
        setting_android.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", appInfo.packageName, null);
            intent.setData(uri);
            context.startActivity(intent);
            return true;
        });
    }

    public static void settings_change_notifications(PreferenceFragmentCompat context) {
        Preference change_notif = context.findPreference("setting_notifications");
        assert change_notif != null;

        change_notif.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireContext(), NotificationSettings.class);
            context.startActivity(intent);
            return true;
        });
    }

    public static void settings_notification_history(PreferenceFragmentCompat context) {
        Preference setting_notification_history = context.findPreference("setting_notification_history");
        assert setting_notification_history != null;

        setting_notification_history.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), NotificationActivity.class));
            return true;
        });
    }

    public static void settings_change_theme(PreferenceFragmentCompat context) {
        ListPreference theme_preference = context.findPreference("setting_change_theme");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(context.requireContext());
        assert sharedPreferences != null;
        String selectedTheme = sharedPreferences.getString("theme", "follow_system");

        assert theme_preference != null;
        theme_preference.setValue(selectedTheme);
        theme_preference.setSummary(theme_preference.getEntry());

        theme_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("theme", (String) newValue).apply();
            switch((String) newValue){
                case "no":
                    theme_preference.setSummary(context.getString(R.string.theme_light));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "yes":
                    theme_preference.setSummary(context.getString(R.string.theme_dark));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    theme_preference.setSummary(context.getString(R.string.theme_default));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            return true;
        });
    }

    public static void settings_security(PreferenceFragmentCompat context) {
        Preference setting_security = context.findPreference("setting_security");
        assert setting_security != null;

        setting_security.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.getActivity(), SettingsActivity.class);
            intent.putExtra("fragment", SecuritySettings.class.getCanonicalName());
            intent.putExtra("title", context.getString(R.string.security));
            context.requireActivity().startActivity(intent);
            return true;
        });
    }

    public static void settings_change_password(PreferenceFragmentCompat context) {
        Preference change_password = context.findPreference("setting_change_password");

        assert change_password != null;
        change_password.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), ChangePasswordActivity.class));
            return true;
        });
    }

    public static void setting_require_biometric(PreferenceFragmentCompat context) {
        SwitchPreference setting_require_biometric = context.findPreference("setting_require_biometric");
        assert setting_require_biometric != null;
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(context.requireContext());
        assert sharedPreferences != null;

        int bioType = BiometricAuthUtil.isAuthenticationSupported(context.requireContext());
        if (bioType != BiometricManager.BIOMETRIC_SUCCESS && !(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && bioType == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)){
            Objects.requireNonNull(setting_require_biometric.getParent()).removePreference(setting_require_biometric);
        }

        setting_require_biometric.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false));

        setting_require_biometric.setOnPreferenceChangeListener((preference, newValue) -> {
            if(newValue.toString().equals("true")){
                int bioType2 = BiometricAuthUtil.isAuthenticationSupported(context.requireContext());
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && bioType2 == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED){
                    BiometricAuthUtil.createBiometricCredentials(context);
                    return false;
                } else if (bioType2 == BiometricManager.BIOMETRIC_SUCCESS){
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, true).apply();
                } else
                    return false;
            } else if(newValue.toString().equals("false")){
                sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false).apply();
            }
            return true;
        });
    }
}
