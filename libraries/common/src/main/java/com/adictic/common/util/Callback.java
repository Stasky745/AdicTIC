package com.adictic.common.util;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Response;

public class Callback<T> implements retrofit2.Callback<T> {

    private int retryCountLastApp = 0;
    private final int TOTAL_RETRIES = 5;
    private final int delayMillis = 1000;

    @Override
    public void onResponse(@NonNull Call<T> call,@NonNull Response<T> response) {
        if(!response.isSuccessful() && response.code()>=500 && retryCountLastApp++ < TOTAL_RETRIES)
            //Tornar a provar nomès si és error de servidor.
            retryFailedCall(this, call, delayMillis*retryCountLastApp);
    }

    @Override
    public void onFailure(@NonNull Call<T> call,@NonNull Throwable t) {
        if(retryCountLastApp++ < TOTAL_RETRIES)
            retryFailedCall(this, call, delayMillis*retryCountLastApp);
    }

    private static <T> void retryFailedCall(Callback<T> callback, Call<T> call, int delayMillis){
        wait(delayMillis);
        call.clone().enqueue(callback);
    }

    private static void wait(int delayMillis){
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
