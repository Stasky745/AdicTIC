package com.adictic.client.ui.setting;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.adictic.client.BuildConfig;
import com.adictic.client.R;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.hilt.AdicticEntryPoint;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.util.Constants;
import com.adictic.common.util.HiltEntryPoint;
import com.adictic.common.util.hilt.Repository;

import dagger.hilt.EntryPoints;
import dagger.hilt.android.EntryPointAccessors;

public class MainSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        AdicticEntryPoint mEntryPoint = EntryPointAccessors.fromApplication(requireContext(), AdicticEntryPoint.class);
        AdicticRepository repository = mEntryPoint.getAdicticRepository();

        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;
        if (!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false)) {
            // Settings de fill
            setPreferencesFromResource(R.xml.settings_child, rootKey);
            repository.settings_permission(this);
            if(BuildConfig.DEBUG) {
                repository.settings_change_theme(this);
                repository.settings_tancar_sessio(this);
                repository.settings_pujar_informe(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceChild");
                PreferenceCategory myPrefCat = findPreference("pcdebug");
                if (preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }

        } else {
            // Settings de pare
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            repository.settings_tancar_sessio(this);
            repository.settings_notification_history(this);
            repository.settings_security(this);
            repository.settings_android(this);
            repository.settings_report_bug(this);
            repository.settings_report_suggestion(this);
            if(BuildConfig.DEBUG){
                repository.settings_change_theme(this);
            } else {
                PreferenceScreen preferenceScreen = findPreference("preferenceParent");
                PreferenceCategory myPrefCat = findPreference("ppdebug");
                if(preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
            }
        }
        repository.settings_change_notifications(this);
        repository.settings_change_language(this);
    }

}
