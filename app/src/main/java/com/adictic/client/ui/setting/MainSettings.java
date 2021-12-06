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

public class MainSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());
        assert sharedPreferences != null;
        if (!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false)) {
            // Settings de fill
            setPreferencesFromResource(R.xml.settings_child, rootKey);
            FuncionsSettings.settings_permission(this);
            if(BuildConfig.DEBUG) {
                FuncionsSettings.settings_change_theme(this);
                FuncionsSettings.settings_tancar_sessio(this);
                FuncionsSettings.settings_pujar_informe(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceChild");
                PreferenceCategory myPrefCat = findPreference("pcdebug");
                if (preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }

        } else {
            // Settings de pare
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            FuncionsSettings.settings_tancar_sessio(this);
            FuncionsSettings.settings_notification_history(this);
            FuncionsSettings.settings_security(this);
            FuncionsSettings.settings_android(this);
            FuncionsSettings.settings_report_bug(this);
            FuncionsSettings.settings_report_suggestion(this);
            if(BuildConfig.DEBUG){
                FuncionsSettings.settings_change_theme(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceParent");
                PreferenceCategory myPrefCat = findPreference("ppdebug");
                if(preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }
        }
        FuncionsSettings.settings_change_notifications(this);
        FuncionsSettings.settings_change_language(this);
    }

}
