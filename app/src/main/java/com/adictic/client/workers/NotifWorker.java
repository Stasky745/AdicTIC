package com.adictic.client.workers;

import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.entity.NotificationInformation;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.common.util.Callback;

import retrofit2.Call;
import retrofit2.Response;

public class NotifWorker extends Worker {
    private int valid;

    public NotifWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
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

        valid = 0;

        Call<String> call = api.sendNotification(idChild, notif);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful() && response.body() != null)
                    valid = 1;
                else
                    valid = -1;
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                valid = -1;
            }
        });
        long start = System.currentTimeMillis();
        long delay = System.currentTimeMillis() - start;

        while(valid == 0 && delay < 5000) {
            SystemClock.sleep(500);
            delay = System.currentTimeMillis() - start;
        }

        if(valid == 1)
            return Result.success();
        else
            return Result.retry();
    }
}
