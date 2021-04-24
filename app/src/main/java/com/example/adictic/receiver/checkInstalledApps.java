package com.example.adictic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.adictic.entity.AppInfo;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class checkInstalledApps extends BroadcastReceiver {
    private final String TAG = "checkInstalledApps (BroadcastReceiver)";
    private TodoApi mTodoService;
    private SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        mTodoService = ((TodoApp) context.getApplicationContext()).getAPI();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(context);
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            String pkgName = intent.getDataString();
            Log.i(TAG,"S'intenta afegir l'app: " + pkgName);
            enviarAppInstall(context, pkgName);
        }
        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
            String pkgName = intent.getDataString();
            Log.i(TAG,"S'intenta esborrar l'app: " + pkgName);
            enviarAppUninstall(pkgName);
        }
    }

    private void enviarAppInstall(Context context, String pkgName){
        try {
            PackageManager mPm = context.getPackageManager();
            ApplicationInfo applicationInfo = mPm.getApplicationInfo(pkgName,PackageManager.GET_META_DATA);

            AppInfo appInfo = new AppInfo();
            appInfo.pkgName = pkgName;
            appInfo.appName = mPm.getApplicationLabel(applicationInfo).toString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category = applicationInfo.category;
            }

            Call<String> call = mTodoService.postAppInstalled(sharedPreferences.getLong("userId",-1),appInfo);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if(response.isSuccessful()) Log.i(TAG,"S'ha enviat l'app Instal·lada correctament.");
                    else Log.i(TAG,"No s'ha pogut enviar l'app Instal·lada correctament.");
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i(TAG,"No s'ha pogut enviar l'app Instal·lada correctament.");
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void enviarAppUninstall(String pkgName){
        Call<String> call = mTodoService.postAppUninstalled(sharedPreferences.getLong("userId",-1),pkgName);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful())
                    Log.i(TAG, "S'ha enviat l'app desinstal·lada correctament.");
                else Log.i(TAG, "No s'ha pogut enviar l'app desinstal·lada correctament.");
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i(TAG, "No s'ha pogut enviar l'app desinstal·lada correctament.");
            }
        });
    }
}