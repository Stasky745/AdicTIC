package com.example.adictic.workers.event_workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.EventBlock;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class StartBlockEventWorker extends Worker {
    public StartBlockEventWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        assert sharedPreferences != null;
        int activeEvents = sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,0);
        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, activeEvents + 1).apply();

//        // Agafem l'event del repositori
//        List<EventBlock> list = Funcions.readFromFile(getApplicationContext(), Constants.FILE_EVENT_BLOCK,false);
//        EventBlock eventBlock = list.get(list.indexOf(id));
//        eventBlock.activeNow = true;
//        int endTime = eventBlock.endEvent;
//
//        //Ens guardem l'hora a què acaba l'event
//        DateTime dateTime = new DateTime()
//                .withMillisOfDay(endTime);
//
//        Funcions.write2File(getApplicationContext(),list);

        return Result.success();
    }
}
