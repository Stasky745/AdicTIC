package com.adictic.client.ui.setting;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.adictic.client.BuildConfig;
import com.adictic.client.R;
import com.adictic.client.util.Funcions;
import com.adictic.common.util.Constants;
import com.adictic.common.util.HiltEntryPoint;
import com.adictic.common.util.hilt.Repository;

import dagger.hilt.EntryPoints;

public class MainSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        HiltEntryPoint mEntryPoint = EntryPoints.get(requireContext(), HiltEntryPoint.class);
        Repository repository = mEntryPoint.getRepository();

        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;
        if (!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false)) {
            // Settings de fill
            setPreferencesFromResource(R.xml.settings_child, rootKey);
            FuncionsAppSettings.settings_permission(this);
            if(BuildConfig.DEBUG) {
                FuncionsAppSettings.settings_change_theme(this);
                FuncionsAppSettings.settings_tancar_sessio(this);
                FuncionsAppSettings.settings_pujar_informe(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceChild");
                PreferenceCategory myPrefCat = findPreference("pcdebug");
                if (preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }

        } else {
            // Settings de pare
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            FuncionsAppSettings.settings_tancar_sessio(this);
            FuncionsAppSettings.settings_notification_history(this);
            FuncionsAppSettings.settings_security(this);
            FuncionsAppSettings.settings_android(this);
            FuncionsAppSettings.settings_report_bug(this);
            FuncionsAppSettings.settings_report_suggestion(this);
            if(BuildConfig.DEBUG){
                FuncionsAppSettings.settings_change_theme(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceParent");
                PreferenceCategory myPrefCat = findPreference("ppdebug");
                if(preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }
        }
        FuncionsAppSettings.settings_change_notifications(this);
        FuncionsAppSettings.settings_change_language(this);
    }

}
