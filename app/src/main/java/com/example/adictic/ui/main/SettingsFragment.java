package com.example.adictic.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.example.adictic.R;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.inici.Login;
import com.example.adictic.ui.inici.SplashScreen;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends PreferenceFragmentCompat {

    private TodoApi mTodoService;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mTodoService = ((TodoApp) requireActivity().getApplication()).getAPI();

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());
        if (!sharedPreferences.getBoolean("isTutor", false)) {
            setPreferencesFromResource(R.xml.settings_child, rootKey);
            if(sharedPreferences.getBoolean("debug",false)) {
                settings_tancar_sessio();
                settings_pujar_informe();
            } else {
                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceChild");
                PreferenceCategory myPrefCat = (PreferenceCategory) findPreference("pcdebug");
                preferenceScreen.removePreference(myPrefCat);
            }

        } else {
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            settings_tancar_sessio();
        }
        settings_change_theme();
        settings_change_language();
    }

    private void settings_pujar_informe(){
        Preference pujar_informe = findPreference("setting_pujar_informe");

        assert pujar_informe != null;
        pujar_informe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                List<GeneralUsage> gul = Funcions.getGeneralUsages(getContext(), TodoApp.getDayOfYear(), Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

                Funcions.canviarMesosAServidor(gul);

                Call<String> call = mTodoService.sendAppUsage(sharedPreferences.getLong("idUser",-1), gul);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {}

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {}
                });
                return true;
            }
        });

    }

    private void settings_change_language() {
        sharedPreferences = getPreferenceManager().getSharedPreferences();

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

    private void settings_change_theme() {
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        ListPreference theme_preference = findPreference("setting_change_theme");
        String selectedTheme = sharedPreferences.getString("theme", "follow_system");

        assert theme_preference != null;
        theme_preference.setValue(selectedTheme);
        theme_preference.setSummary(theme_preference.getEntry());

        theme_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("theme", (String) newValue).apply();
            switch((String) newValue){
                case "no":
                    theme_preference.setSummary(getString(R.string.theme_light));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "yes":
                    theme_preference.setSummary(getString(R.string.theme_dark));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    theme_preference.setSummary(getString(R.string.theme_default));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            return true;
        });
    }
}
