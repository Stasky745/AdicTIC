package com.adictic.common.util;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Response;

public class Callback<T> implements retrofit2.Callback<T> {

    private int retryCountLastApp = 0;
    private final int TOTAL_RETRIES = 5;
    private final int delayMillis = 2000;

    @Override
    public void onResponse(@NonNull Call<T> call,@NonNull Response<T> response) {
        if(!response.isSuccessful() && retryCountLastApp++ < TOTAL_RETRIES)
            Funcions.retryFailedCall(this, call, delayMillis);
    }

    @Override
    public void onFailure(@NonNull Call<T> call,@NonNull Throwable t) {
        if(retryCountLastApp++ < TOTAL_RETRIES)
            Funcions.retryFailedCall(this, call, delayMillis);
    }
}
