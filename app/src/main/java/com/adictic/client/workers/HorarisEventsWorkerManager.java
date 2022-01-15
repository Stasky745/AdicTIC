package com.adictic.client.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.util.Funcions;
import com.adictic.common.database.EventDatabase;
import com.adictic.common.database.HorarisDatabase;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.util.Constants;

import org.joda.time.DateTime;

import java.util.ArrayList;
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

        if(!Funcions.accessibilityServiceOn(getApplicationContext()))
            return Result.failure();

        long millisAra = DateTime.now().getMillis();
        long millisAvui = DateTime.now().withTimeAtStartOfDay().getMillis();
        long millisDema = DateTime.now().plusDays(1).withTimeAtStartOfDay().getMillis();

        boolean esDiaNou = (millisAra - millisAvui) < (millisDema - millisAvui);

        if(!esDiaNou) {
            return Result.retry();
        }

        // Agafem i apliquem horaris
        HorarisDatabase horarisDatabase = Room.databaseBuilder(getApplicationContext(),
                HorarisDatabase.class, Constants.ROOM_HORARIS_DATABASE)
                .enableMultiInstanceInvalidation()
                .build();

        List<HorarisNit> horarisNit = new ArrayList<>(horarisDatabase.horarisNitDao().getAll());

        horarisDatabase.close();

        Funcions.setHoraris(getApplicationContext(), horarisNit);

        // Agafem i apliquem Events
        EventDatabase eventDatabase = Room.databaseBuilder(getApplicationContext(),
                EventDatabase.class, Constants.ROOM_EVENT_DATABASE)
                .enableMultiInstanceInvalidation()
                .build();

        List<EventBlock> eventList = new ArrayList<>(eventDatabase.eventBlockDao().getAll());

        eventDatabase.close();

        Funcions.setEvents(getApplicationContext(), eventList);

        return Result.success();
    }
}
