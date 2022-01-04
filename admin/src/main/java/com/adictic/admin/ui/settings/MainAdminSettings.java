package com.adictic.admin.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.adictic.admin.R;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;

public class MainAdminSettings extends PreferenceFragmentCompat {

    private final String TAG = "MainAdminSettings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_admin, rootKey);

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(requireContext());
        assert sharedPreferences != null;
        if(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_IS_SUPERADMIN, false)) {
            PreferenceScreen preferenceScreen = findPreference("preferenceAdmin");
            PreferenceCategory myPrefCat = findPreference("ppsuperAdmin");
            if (preferenceScreen != null) preferenceScreen.removePreference(myPrefCat);
        } else {
            FuncionsAdminSettings.settings_create_admin(this);
        }

        FuncionsAdminSettings.settings_change_language(this);
        FuncionsAdminSettings.settings_security(this);
        FuncionsAdminSettings.settings_change_notifications(this);
        FuncionsAdminSettings.settings_notification_history(this);
        FuncionsAdminSettings.settings_android(this);
        FuncionsAdminSettings.settings_tancar_sessio(this);
    }
}
