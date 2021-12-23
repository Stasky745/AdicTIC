package com.adictic.client.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.adictic.client.entity.NotificationInformation;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.common.util.Callback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import retrofit2.Call;
import retrofit2.Response;

public class NotifWorker extends ListenableWorker {
    public NotifWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();

        Data data = getInputData();

        NotificationInformation notif = new NotificationInformation();

        notif.title = data.getString("title");
        notif.message = data.getString("body");
        notif.dateMillis = data.getLong("dateMillis", System.currentTimeMillis());
        notif.important = data.getBoolean("important", true);
        notif.notifCode = data.getString("notifCode");
        notif.read = false;

        Long idChild = data.getLong("idChild", -1);

        AdicticApi api = ((AdicticApp) getApplicationContext()).getAPI();

        Call<String> call = api.sendNotification(idChild, notif);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                future.set(Result.success());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                future.set(Result.retry());
            }
        });

        return future;
    }
}
