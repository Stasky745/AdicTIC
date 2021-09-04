package com.example.adictic_admin.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.adictic.common.entity.User;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.example.adictic_admin.BuildConfig;
import com.example.adictic_admin.MainActivity;
import com.example.adictic_admin.R;
import com.example.adictic_admin.rest.AdminApi;
import com.example.adictic_admin.util.AdminApp;
import com.example.adictic_admin.util.Funcions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {
    private final static String TAG = "SplashScreen";
    private SharedPreferences sharedPreferences;
    private String token = "";
    private AdminApi adminApi;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;
        adminApi = ((AdminApp) this.getApplication()).getAPI();
        checkForUpdates();
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

                    Call<User> call = adminApi.checkWithToken(Crypt.getAES(token));
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    super.onResponse(call, response);
                            if (response.isSuccessful() && response.body() != null) {
                                User adminCheckWithToken = response.body();
                                sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER, adminCheckWithToken.id).apply();
                                sharedPreferences.edit().putLong(Constants.SHARED_PREFS_ID_ADMIN, adminCheckWithToken.adminId).apply();
                                SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
                            } else {
                                SplashScreen.this.startActivity(new Intent(SplashScreen.this, Login.class));
                            }
                            SplashScreen.this.finish();
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

    private void checkForUpdates() {
        if(BuildConfig.DEBUG){
            startApp();
            return;
        }
        Call<String> call = adminApi.checkForUpdates(BuildConfig.VERSION_NAME);
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
        String apkName = "adictic-admin_"+newVersion+".apk";
        File file = new File(PATH,apkName);
        System.out.println(file.getAbsolutePath());
        if(file.exists()){
            installApk(file);
            return;
        }
        getUpdateFromServer();
    }

    private void getUpdateFromServer() {
        Call<ResponseBody> call = adminApi.getLatestVersion();
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
                toast.show(); }
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
        SplashScreen.this.startActivityForResult(intent,1034);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1034)
        {
            startApp();
        }
    }
}
