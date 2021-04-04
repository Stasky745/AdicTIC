package com.example.adictic.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.Login;
import com.example.adictic.activity.SplashScreen;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Crypt;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if(TodoApp.getTutor()==0){
            setPreferencesFromResource(R.xml.settings_child, rootKey);
        }
        else{
            setPreferencesFromResource(R.xml.settings_parent, rootKey);
            settings_tancar_sessio();
        }
        settings_change_language();
    }

    private void settings_change_language() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        ListPreference language_preference = findPreference("setting_change_language");
        String selectedLanguage = sharedPreferences.getString("language","none");
        language_preference.setValue(selectedLanguage);
        language_preference.setSummary(language_preference.getEntry());

        language_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sharedPreferences.edit().putString("language", (String) newValue).apply();

                Intent refresh = new Intent(getActivity(), SplashScreen.class);
                getActivity().finish();
                startActivity(refresh);

                return true;
            }
        });
    }

    private void settings_tancar_sessio(){
        Preference tancarSessio = findPreference("setting_tancar_sessio");
        tancarSessio.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TodoApi mTodoService = ((TodoApp)  getActivity().getApplication()).getAPI();
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {

                            Call<String> call = null;
                            if (!task.isSuccessful()) {
                                call = mTodoService.logout("");
                            } else {
                                // Get new Instance ID token
                                String token = task.getResult();
                                call = mTodoService.logout(Crypt.getAES(token));
                            }
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if (response.isSuccessful()) {
                                        getActivity().startActivity(new Intent(getActivity(), Login.class));
                                        getActivity().finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                }
                            });

                        });
                return true;
            }
        });
    }
}
