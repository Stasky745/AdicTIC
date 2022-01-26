package com.adictic.admin.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.adictic.admin.R;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.ui.Login;
import com.adictic.admin.ui.SplashScreen;
import com.adictic.admin.util.AdminApp;
import com.adictic.admin.util.Funcions;
import com.adictic.admin.util.hilt.AdminEntryPoint;
import com.adictic.admin.util.hilt.AdminRepository;
import com.adictic.common.ui.settings.FuncionsSettings;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

import dagger.hilt.EntryPoints;
import retrofit2.Call;
import retrofit2.Response;

public class FuncionsAdminSettings extends FuncionsSettings {

    public static void settings_change_language(PreferenceFragmentCompat context) {
        AdminEntryPoint mEntryPoint = EntryPoints.get(context.requireContext(), AdminEntryPoint.class);
        AdminRepository repository = mEntryPoint.getRepository();

        ListPreference language_preference = context.findPreference("setting_change_language");
        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();
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
        AdminEntryPoint mEntryPoint = EntryPoints.get(context.requireContext(), AdminEntryPoint.class);
        AdminRepository repository = mEntryPoint.getRepository();

        Preference tancarSessio = context.findPreference("setting_tancar_sessio");
        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;
        AdminApi mTodoService = repository.getApi();

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
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(Constants.SHARED_PREFS_USERNAME,null);
                                    editor.putString(Constants.SHARED_PREFS_PASSWORD,null);
                                    editor.putLong(Constants.SHARED_PREFS_ID_ADMIN, -1);
                                    editor.putBoolean(Constants.SHARED_PREFS_IS_SUPERADMIN, false);
                                    editor.apply();
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

    public static void settings_create_admin(PreferenceFragmentCompat context) {
        Preference crear_admins = context.findPreference("setting_crear_admins");
        assert crear_admins != null;

        crear_admins.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), AdminCreatorActivity.class));
            return true;
        });
    }

}
