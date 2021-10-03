package com.adictic.common.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.common.util.Funcions;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Response;

public class UpdateTokenWorker extends Worker {
    private final static String TAG = "UpdateTokenWorker";
    private Boolean success = null;
    private String token;
    public UpdateTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker start");

        long start = System.currentTimeMillis();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) {
                        success = false;
                        return;
                    }

                    // Get new Instance ID token
                    token = Crypt.getAES(task.getResult());
                    success = true;
                });

        long timeout = System.currentTimeMillis() - start;

        while(success == null && timeout < 1000 * 60) {
            SystemClock.sleep(1000 * 5);
            timeout = System.currentTimeMillis() - start;
        }

        if(success != null && success){
            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
            assert sharedPreferences != null;
            if(sharedPreferences.getString(Constants.SHARED_PREFS_TOKEN, "").equals(token))
                return Result.success();

            long idUser = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            if(idUser == -1)
                return Result.failure();

            success = null;
            start = System.currentTimeMillis();

            Api mTodoService = ((App) getApplicationContext()).getAPI();
            Call<String> call = mTodoService.updateToken(idUser, token);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    success = response.isSuccessful();
                    if(success) {
                        sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, token).apply();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    success = false;
                }
            });

            while(success == null && timeout < 1000 * 60) {
                SystemClock.sleep(1000 * 5);
                timeout = System.currentTimeMillis() - start;
            }

            if(success != null && success)
                return Result.success();
            else
                return Result.retry();
        }
        else{
            return Result.failure();
        }
    }
}
