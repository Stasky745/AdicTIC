package com.adictic.client.ui.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.inici.Login;
import com.adictic.client.ui.inici.Permisos;
import com.adictic.client.ui.inici.SplashScreen;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.ui.ReportActivity;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.common.ui.settings.FuncionsSettings;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class FuncionsAppSettings extends FuncionsSettings {

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

    public static void settings_permission(PreferenceFragmentCompat context) {
        Preference change_perm = context.findPreference("setting_permission");

        Activity ctx = context.requireActivity();

        assert change_perm != null;
        change_perm.setOnPreferenceClickListener(preference -> {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

            final View dialogLayout = ctx.getLayoutInflater().inflate(R.layout.desbloqueig_dialog, null);
            builder.setView(dialogLayout);
            builder.setNegativeButton(ctx.getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            // Posem el text adequat al dialog
            TextView TV_unlock_title = dialogLayout.findViewById(R.id.TV_unlock_title);
            TV_unlock_title.setText(ctx.getString(R.string.permisos));

            TextView TV_unlock_text = dialogLayout.findViewById(R.id.TV_unlock_text);
            TV_unlock_text.setText(ctx.getString(R.string.password));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TV_unlock_text.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            TextView TV_pwd_error = dialogLayout.findViewById(R.id.TV_pwd_error);
            TV_pwd_error.setVisibility(View.INVISIBLE);

            AlertDialog alertDialog = builder.show();

            Button BT_unlock = dialogLayout.findViewById(R.id.BT_dialog_unlock);
            BT_unlock.setOnClickListener(v1 -> {
                TV_pwd_error.setVisibility(View.INVISIBLE);

                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx.getApplicationContext());
                assert sharedPreferences != null;

                EditText ET_unlock_pwd = dialogLayout.findViewById(R.id.ET_unlock_pwd);
                String pwd = Crypt.getSHA256(ET_unlock_pwd.getText().toString());

                Funcions.isPasswordCorrect(ctx, pwd, valid -> {
                    if(valid){
                        Intent intent = new Intent(ctx, Permisos.class);
                        intent.putExtra("settings", true);
                        ctx.startActivity(intent);
                        alertDialog.dismiss();
                    }
                    else
                        TV_pwd_error.setVisibility(View.VISIBLE);
                });
            });
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
                            call = mTodoService.logout(Crypt.getAES(token, Constants.CRYPT_KEY));
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
}
