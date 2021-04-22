package com.example.adictic.service;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.roomdb.EventBlock;
import com.example.adictic.roomdb.RoomRepo;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import org.joda.time.DateTime;

import java.sql.Time;
import java.util.Calendar;

public class StartBlockEventWorker extends Worker {
    public StartBlockEventWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String name = getInputData().getString("name");

        // Agafem l'event del repositori
        RoomRepo roomRepo = new RoomRepo(getApplicationContext());
        EventBlock eventBlock = roomRepo.getEventFromList(name);
        eventBlock.activeNow = true;
        int endTime = eventBlock.endEvent;
        DateTime dateTime = new DateTime()
                .withMillisOfDay(endTime);
        roomRepo.updateEventBlock(eventBlock);

//        HorarisEvents event = Funcions.getEventFromList(name);
//        String eventEnd = event.finish;
//        Pair<Integer, Integer> timeFinish = Funcions.stringToTime(eventEnd);
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, timeFinish.first);
//        cal.set(Calendar.MINUTE, timeFinish.second);

        long delay = dateTime.getMillis() - Calendar.getInstance().getTimeInMillis();

        //TodoApp.addBlockEvent(name);

        Funcions.runFinishBlockEventWorker(getApplicationContext(), name, delay);

        return Result.success();
    }
}
