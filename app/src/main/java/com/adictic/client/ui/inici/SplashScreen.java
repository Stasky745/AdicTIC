package com.adictic.client.ui.inici;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.adictic.client.BuildConfig;
import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.ui.main.NavActivity;
import com.adictic.client.util.hilt.AdicticEntryPoint;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.util.BiometricAuthUtil;
import com.adictic.client.util.Funcions;
import com.adictic.client.util.LocaleHelper;
import com.adictic.common.entity.User;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import dagger.hilt.android.EntryPointAccessors;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    AdicticRepository repository;

    private final static String TAG = "SplashScreen";
    private SharedPreferences sharedPreferences;
    private String token = "";
    private AdicticApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        api = repository.getApi();
        //checkForUpdates(); //TODO: Desactivat de moment
        startApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startApp(){
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

                    Call<User> call = api.checkWithToken(Crypt.getAES(token));
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Firebase Token = " + token);
                                if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false)) {
                                    checkBiometricAuth();
                                }
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

                                        extras.putParcelable("user", usuari);
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
                            super.onFailure(call, t);
                            Toast toast = Toast.makeText(SplashScreen.this, "Error checking login status", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                });
    }

    private void mirarPermisos(){
        PowerManager mPm = (PowerManager) getSystemService(POWER_SERVICE);

        if (!Funcions.isAppUsagePermissionOn(SplashScreen.this) ||
                !Funcions.isAdminPermissionsOn(SplashScreen.this) ||
                !Funcions.isAccessibilitySettingsOn(SplashScreen.this) ||
                !Funcions.isBackgroundLocationPermissionOn(getApplicationContext()) ||
                !mPm.isIgnoringBatteryOptimizations(getPackageName()) ||
                !Settings.canDrawOverlays(SplashScreen.this)) {

            SplashScreen.this.startActivity(new Intent(this, Permisos.class));
        }
            else {
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
        }
        SplashScreen.this.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if(repository == null) {
            AdicticEntryPoint mEntryPoint = EntryPointAccessors.fromApplication(newBase.getApplicationContext(), AdicticEntryPoint.class);
            repository = mEntryPoint.getAdicticRepository();
        }

        sharedPreferences = repository.getEncryptedSharedPreferences();
        assert  sharedPreferences != null;

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
        String lang = sharedPreferences != null ? sharedPreferences.getString("language", "none") : "none";
        if (lang.equals("none"))
            super.attachBaseContext(newBase);
        else
            super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    private void checkForUpdates() {
        if(BuildConfig.DEBUG){
            startApp();
            return;
        }
        Call<String> call = api.checkForUpdates(BuildConfig.VERSION_NAME);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() && response.body()!=null && !response.body().equals("NO")) {
                    installUpdate(response.body());
                }
                else{
                    FilenameFilter filenameFilter = (file, s) -> s.endsWith(".apk");// && !file.isDirectory();
                    File[] list = getExternalCacheDir().listFiles(filenameFilter);
                    if (list != null && list.length > 0) {
                        Arrays.stream(list)
                                .forEach(File::delete);
                    }
                    startApp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(SplashScreen.this, "Error checking login status", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void installUpdate(String newVersion){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        String PATH = getExternalCacheDir().getPath();
        String apkName = "adictic_"+newVersion+".apk";
        File file = new File(PATH,apkName);
        System.out.println(file.getAbsolutePath());
        if(file.exists()){
            installApk(file);
            return;
        }
        getUpdateFromServer();
    }

    private void getUpdateFromServer() {
        Call<ResponseBody> call = api.getLatestVersion();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() && response.body()!=null) {
                    try {
                        String PATH = getExternalCacheDir().getPath();
                        String apkName = response.headers().get("Content-Disposition").split("filename=")[1].replace("\"","");

                        File file = new File(PATH,apkName);
                        if(!file.exists()) {
                            BufferedSink sink = Okio.buffer(Okio.sink(file));
                            sink.writeAll(response.body().source());
                            sink.flush();
                        }
                        installApk(file);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else startApp();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(SplashScreen.this, "Error checking login status", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void installApk(File file){
        PackageManager pkgManager= SplashScreen.this.getPackageManager();
        PackageInfo packageInfo = pkgManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);
        if(packageInfo == null){
            file.delete();
            getUpdateFromServer();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData (FileProvider.getUriForFile(SplashScreen.this, BuildConfig.APPLICATION_ID + ".provider", file));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        resultInstallAPK.launch(intent);
    }

    ActivityResultLauncher<Intent> resultInstallAPK = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                startApp();
            }
        }
    );

    private void checkBiometricAuth(){
        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false)){
            BiometricPrompt.AuthenticationCallback autCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(SplashScreen.this, errString, Toast.LENGTH_LONG).show();
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, Crypt.getAES(token)).apply();
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    intent.putExtra("fromBiometric", true);
                    SplashScreen.this.startActivity(intent);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    goToMainActivity();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Log.e(TAG, "Authentication failed");
                    finish();
                }
            };
            try {
                BiometricAuthUtil.startAuthentication(SplashScreen.this, autCallback);
            } catch (UnsupportedOperationException ex){
                //This shouldn't happen. Disable the setting?
                goToMainActivity();
            }
        } else goToMainActivity();
    }

    private void goToMainActivity(){
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, Crypt.getAES(token)).apply();
        SplashScreen.this.startActivity(new Intent(SplashScreen.this, NavActivity.class));
    }
}
