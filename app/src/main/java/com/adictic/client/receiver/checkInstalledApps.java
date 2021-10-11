package com.adictic.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;

import retrofit2.Call;
import retrofit2.Response;

public class checkInstalledApps extends BroadcastReceiver {
    private final String TAG = "checkInstalledApps (BroadcastReceiver)";
    private AdicticApi mTodoService;
    private SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        mTodoService = ((AdicticApp) context.getApplicationContext()).getAPI();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(context);
        String pkgName = intent.getData().getEncodedSchemeSpecificPart();
        if(intent.getComponent() != null && intent.getComponent().getPackageName().equals(context.getPackageName())) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                Log.i(TAG, "S'intenta afegir l'app: " + pkgName);
                enviarAppInstall(context, pkgName);
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                Log.i(TAG, "S'intenta esborrar l'app: " + pkgName);
                enviarAppUninstall(pkgName);
            }
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

            Call<String> call = mTodoService.postAppInstalled(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1),appInfo);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                    if(response.isSuccessful())
                        Log.i(TAG,"S'ha enviat l'app Instal·lada correctament.");
                    else
                        Log.i(TAG,"No s'ha pogut enviar l'app Instal·lada correctament.");
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                    Log.i(TAG,"No s'ha pogut enviar l'app Instal·lada correctament.");
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void enviarAppUninstall(String pkgName){
        Call<String> call = mTodoService.postAppUninstalled(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1),pkgName);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if (response.isSuccessful())
                    Log.i(TAG, "S'ha enviat l'app desinstal·lada correctament.");
                else
                    Log.i(TAG, "No s'ha pogut enviar l'app desinstal·lada correctament.");
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Log.i(TAG, "No s'ha pogut enviar l'app desinstal·lada correctament.");
            }
        });
    }
}
