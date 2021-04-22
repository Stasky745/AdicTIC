package com.example.adictic.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.adictic.R;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.inici.Login;
import com.example.adictic.ui.inici.SplashScreen;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());
        if (!sharedPreferences.getBoolean("isTutor",false)) {
            setPreferencesFromResource(R.xml.settings_child, rootKey);
        } else {
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            settings_tancar_sessio();
        }
        settings_change_language();
    }

    private void settings_change_language() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        ListPreference language_preference = findPreference("setting_change_language");
        String selectedLanguage = sharedPreferences.getString("language", "none");

        assert language_preference != null;
        language_preference.setValue(selectedLanguage);
        language_preference.setSummary(language_preference.getEntry());

        language_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("language", (String) newValue).apply();

            Intent refresh = new Intent(getActivity(), SplashScreen.class);
            requireActivity().finish();
            startActivity(refresh);

            return true;
        });
    }

    private void settings_tancar_sessio() {
        Preference tancarSessio = findPreference("setting_tancar_sessio");

        assert tancarSessio != null;
        tancarSessio.setOnPreferenceClickListener(preference -> {
            TodoApi mTodoService = ((TodoApp) requireActivity().getApplication()).getAPI();
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
                                if (response.isSuccessful()) {
                                    requireActivity().startActivity(new Intent(getActivity(), Login.class));
                                    requireActivity().finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            }
                        });

                    });
            return true;
        });
    }
}
