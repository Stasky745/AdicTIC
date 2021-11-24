package com.adictic.client.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.inici.Login;
import com.adictic.client.ui.inici.Permisos;
import com.adictic.client.ui.inici.SplashScreen;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.BiometricAuthUtil;
import com.adictic.client.util.Funcions;
import com.adictic.common.ui.ReportActivity;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class FuncionsSettings {

    public static void settings_report_suggestion(PreferenceFragmentCompat context) {
        Preference report_suggestion = context.findPreference("setting_report_suggestion");

        assert report_suggestion != null;
        report_suggestion.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireActivity(), ReportActivity.class);
            intent.putExtra("isTypeBug", false);
            context.startActivity(intent);
            return true;
        });
    }

    public static void settings_report_bug(PreferenceFragmentCompat context) {
        Preference report_bug = context.findPreference("setting_report_bug");

        assert report_bug != null;
        report_bug.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireActivity(), ReportActivity.class);
            intent.putExtra("isTypeBug", true);
            context.startActivity(intent);
            return true;
        });
    }

    public static void settings_change_notifications(PreferenceFragmentCompat context) {
        Preference change_notif = context.findPreference("setting_notifications");
        ApplicationInfo appInfo = context.requireContext().getApplicationContext().getApplicationInfo();

        assert change_notif != null;
        change_notif.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

            //for Android 5-7
            intent.putExtra("app_package", appInfo.packageName);
            intent.putExtra("app_uid", appInfo.uid);

            // for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", appInfo.packageName);

            context.startActivity(intent);
            return true;
        });

    }

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

    public static void settings_permission(PreferenceFragmentCompat context) {
        Preference change_perm = context.findPreference("setting_permission");

        assert change_perm != null;
        change_perm.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireActivity(), Permisos.class);
            context.startActivity(intent);
            return true;
        });
    }

    public static void settings_pujar_informe(PreferenceFragmentCompat context) {
        Preference pujar_informe = context.findPreference("setting_pujar_informe");

        assert pujar_informe != null;
        pujar_informe.setOnPreferenceClickListener(preference -> {
            Funcions.sendAppUsage(context.requireContext());
            return true;
        });

    }

    public static void settings_change_language(PreferenceFragmentCompat context) {
        ListPreference language_preference = context.findPreference("setting_change_language");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(context.requireContext());
        assert sharedPreferences != null;
        String selectedLanguage = sharedPreferences.getString("language", Locale.getDefault().getLanguage());

        assert language_preference != null;
        language_preference.setValue(selectedLanguage);
        if(language_preference.getEntry() == null || language_preference.getEntry().length()==0) language_preference.setSummary(context.getString(R.string.language_not_supported));
        else language_preference.setSummary(language_preference.getEntry());

        language_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("language", (String) newValue).apply();

            Intent refresh = new Intent(context.requireActivity(), SplashScreen.class);
            context.requireActivity().finish();
            context.startActivity(refresh);

            return true;
        });
    }

    public static void settings_tancar_sessio(PreferenceFragmentCompat context) {
        Preference tancarSessio = context.findPreference("setting_tancar_sessio");
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(context.requireContext());
        assert sharedPreferences != null;
        AdicticApi mTodoService = ((AdicticApp) context.requireActivity().getApplication()).getAPI();

        assert tancarSessio != null;
        tancarSessio.setOnPreferenceClickListener(preference -> {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {

                        Call<String> call;
                        if (!task.isSuccessful()) {
                            call = mTodoService.logout("");
                        } else {
                            // Get new Instance ID token
                            String token = task.getResult();
                            call = mTodoService.logout(Crypt.getAES(token));
                        }
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                super.onResponse(call, response);
                                if (response.isSuccessful()) {
                                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_USERNAME,null).apply();
                                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD,null).apply();
                                    context.requireActivity().startActivity(new Intent(context.getActivity(), Login.class));
                                    context.requireActivity().finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                super.onFailure(call, t);
                            }
                        });

                    });
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

    public static void settings_change_password(PreferenceFragmentCompat context) {
        Preference change_password = context.findPreference("setting_change_password");

        assert change_password != null;
        change_password.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), ChangePasswordActivity.class));
            return true;
        });
    }

    public static void settings_security(PreferenceFragmentCompat context) {
        Preference setting_security = context.findPreference("setting_security");
        assert setting_security != null;

        setting_security.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.getActivity(), SettingsActivity.class);
            intent.putExtra("fragment", "security");
            intent.putExtra("title", context.getString(R.string.security));
            context.requireActivity().startActivity(intent);
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
                    setting_require_biometric.setChecked(false);
                } else if (bioType2 == BiometricManager.BIOMETRIC_SUCCESS){
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, true).apply();
                } else {
                    setting_require_biometric.setChecked(false);
                }
            } else if(newValue.toString().equals("false")){
                sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false).apply();
            }
            return true;
        });
    }
}
