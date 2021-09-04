package com.adictic.common.workers;

import android.content.Context;
import android.content.SharedPreferences;
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

import retrofit2.Call;
import retrofit2.Response;

public class UpdateTokenWorker extends Worker {
    private final static String TAG = "UpdateTokenWorker";
    private boolean success;
    public UpdateTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker start");

        long idUser = getInputData().getLong("idUser", -1);
        String token = getInputData().getString("token");
        Api mTodoService = ((App) getApplicationContext()).getAPI();

        Call<String> call = mTodoService.updateToken(idUser, Crypt.getAES(token));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                success = response.isSuccessful();
                if(success) {
                    SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
                    assert sharedPreferences != null;
                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, Crypt.getAES(token)).apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                success = false;
            }
        });

        if(!success) {
            Log.d(TAG, "No s'ha pogut penjar Token al servidor.");
            long delay = 1000 * 60 * 5; // Tornar a provar en 5min
            Funcions.runUpdateTokenWorker(getApplicationContext(), idUser, token, delay);
        }

        Log.d(TAG, "Acabar 'doWork'");

        return Result.success();
    }
}
