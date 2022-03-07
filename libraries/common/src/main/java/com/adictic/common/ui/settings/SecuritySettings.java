package com.adictic.common.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.adictic.common.R;

public class SecuritySettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_security_parent, rootKey);
        FuncionsSettings.settings_change_password(this);
        FuncionsSettings.setting_require_biometric(this);
    }
}
