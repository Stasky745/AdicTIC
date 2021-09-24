package com.adictic.client.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.util.Constants;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class HorarisEventsWorkerManager extends Worker {
    private final static String TAG = "HorarisEventsWorkerManager";

    public HorarisEventsWorkerManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"Worker començat");

        if(!Funcions.accessibilityServiceOn())
            return Result.failure();

        long millisAra = DateTime.now().getMillis();
        long millisAvui = DateTime.now().withTimeAtStartOfDay().getMillis();
        long millisDema = DateTime.now().plusDays(1).withTimeAtStartOfDay().getMillis();

        boolean esDiaNou = (millisAra - millisAvui) < (millisDema - millisAvui);

        if(!esDiaNou) {
            return Result.retry();
        }

        Funcions.checkHoraris(getApplicationContext());

        return Result.success();
    }
}
