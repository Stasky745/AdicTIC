package com.example.adictic.ui.inici;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.adictic.R;
import com.example.adictic.entity.User;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.ui.permisos.AccessibilityPermActivity;
import com.example.adictic.ui.permisos.AppUsagePermActivity;
import com.example.adictic.ui.permisos.BackgroundLocationPerm;
import com.example.adictic.ui.permisos.DevicePolicyAdmin;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Crypt;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.LocaleHelper;
import com.example.adictic.util.TodoApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    private final static String TAG = "SplashScreen";
    private SharedPreferences sharedPreferences;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putBoolean("debug",getIntent().getBooleanExtra("debug",false)).apply();
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TodoApi todoApi = ((TodoApp) this.getApplication()).getAPI();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Firebase task is unsuccessful.");
                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, Login.class));
                        SplashScreen.this.finish();
                        return;
                    }
                    token = task.getResult();

                    Log.d(TAG,"Firebase Token: " + token);
                    Log.d(TAG,"Firebase Token (Encrypted): " + Crypt.getAES(token));

                    Call<User> call = todoApi.checkWithToken(Crypt.getAES(token));
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

                            if (response.isSuccessful()) {
                                Log.d(TAG, "Firebase Token = " + token);
                                if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
                                    SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                else if (!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false) && sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1) > 0)
                                    mirarPermisos();
                                else {
                                    User usuari = response.body();
                                    assert usuari != null;

                                    if(usuari.tutor == 1) {
                                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR, true).apply();
                                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER, usuari.id).apply();
                                    }
                                    else {
                                        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ISTUTOR,false).apply();
                                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDTUTOR, usuari.id).apply();
                                    }

                                    if (usuari.tutor == 0 && !usuari.llista.isEmpty()) {
                                        sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER, usuari.llista.get(0).idChild).apply();
                                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                        SplashScreen.this.finish();
                                    } else if (usuari.tutor == 0) {
                                        Bundle extras = new Bundle();

                                        extras.putSerializable("user", usuari);
                                        extras.putString("token", token);
                                        extras.putLong("id", usuari.id);

                                        Intent i = new Intent(SplashScreen.this, NomFill.class);
                                        i.putExtras(extras);

                                        SplashScreen.this.startActivity(i);
                                        SplashScreen.this.finish();
                                    } else {
                                        SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                                        SplashScreen.this.finish();
                                    }
                                }
                            } else {
                                SplashScreen.this.startActivity(new Intent(SplashScreen.this, Login.class));
                                SplashScreen.this.finish();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                            Toast toast = Toast.makeText(SplashScreen.this, "Error checking login status", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                });
    }

    private void mirarPermisos(){
        if (!Funcions.isAppUsagePermissionOn(SplashScreen.this)) {
            SplashScreen.this.startActivity(new Intent(SplashScreen.this, AppUsagePermActivity.class));
            SplashScreen.this.finish();
        } else {
            Funcions.startAppUsageWorker24h(getApplicationContext());
            if (!Funcions.isAdminPermissionsOn(SplashScreen.this)) {
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, DevicePolicyAdmin.class));
                SplashScreen.this.finish();
            } else if (!Funcions.isAccessibilitySettingsOn(SplashScreen.this)) {
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, AccessibilityPermActivity.class));
                SplashScreen.this.finish();
            }
            else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                this.startActivity(new Intent(this, BackgroundLocationPerm.class));
            else {
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
                SplashScreen.this.finish();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        sharedPreferences = Funcions.getEncryptedSharedPreferences(newBase);
        String selectedTheme = sharedPreferences.getString("theme", "follow_system");
        switch(selectedTheme){
            case "no":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "yes":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        String lang = sharedPreferences.getString("language", "none");
        if (lang.equals("none")) super.attachBaseContext(newBase);
        else super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }
}
