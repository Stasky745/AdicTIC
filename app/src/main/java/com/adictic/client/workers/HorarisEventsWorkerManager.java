package com.adictic.client.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.util.hilt.AdicticEntryPoint;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.HorarisNit;

import org.joda.time.DateTime;

import java.util.List;

import dagger.hilt.EntryPoints;

public class HorarisEventsWorkerManager extends Worker {
    private final static String TAG = "HorarisEventsWorkerManager";

    AdicticRepository repository;

    public HorarisEventsWorkerManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        repository = EntryPoints.get(getApplicationContext(), AdicticEntryPoint.class).getAdicticRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"Worker començat");

        if(!repository.accessibilityServiceOn())
            return Result.failure();

        long millisAra = DateTime.now().getMillis();
        long millisAvui = DateTime.now().withTimeAtStartOfDay().getMillis();
        long millisDema = DateTime.now().plusDays(1).withTimeAtStartOfDay().getMillis();

        boolean esDiaNou = (millisAra - millisAvui) < (millisDema - millisAvui);

        if(!esDiaNou) {
            return Result.retry();
        }

        // Agafem i apliquem horaris
        List<HorarisNit> horarisNit = repository.getAllHoraris();

        repository.setHoraris(horarisNit);

        // Agafem i apliquem Events
        List<EventBlock> eventList = repository.getAllEvents();

        repository.setEvents(eventList);

        return Result.success();
    }
}
