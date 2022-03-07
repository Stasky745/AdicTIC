package com.adictic.common.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.common.util.Funcions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Response;

public class UpdateTokenWorker extends ListenableWorker {
    private final static String TAG = "UpdateTokenWorker";
    private String token;
    public UpdateTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) {
                        future.set(Result.failure());
                        return;
                    }

                    // Get new Instance ID token
                    token = Crypt.getAES(task.getResult(), Constants.CRYPT_KEY);
                });

        if(future.isDone())
            return future;

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;
        if(sharedPreferences.getString(Constants.SHARED_PREFS_TOKEN, "").equals(token))
            future.set(Result.success());

        long idUser = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
        if(idUser == -1)
            future.set(Result.failure());

        if(future.isDone())
            return future;

        Api mTodoService = ((App) getApplicationContext()).getAPI();
        Call<String> call = mTodoService.updateToken(idUser, token);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful() && response.body() != null) {
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, token).apply();
                    future.set(Result.success());
                }
                else
                    future.set(Result.retry());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                future.set(Result.retry());
            }
        });

        return future;
    }
}
