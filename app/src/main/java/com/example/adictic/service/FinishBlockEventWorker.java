package com.example.adictic.service;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.List;

public class FinishBlockEventWorker extends Worker {
    public FinishBlockEventWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String name = getInputData().getString("name");
        HorarisEvents event = Funcions.getEventFromList(name);
        String eventStart = event.start;
        Pair<Integer,Integer> timeStart = Funcions.stringToTime(eventStart);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,1);
        cal.set(Calendar.HOUR_OF_DAY,timeStart.first);
        cal.set(Calendar.MINUTE,timeStart.second);

        long delay = cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        TodoApp.removeBlockEvent(name);

        Funcions.runStartBlockEventWorker(getApplicationContext(),name,delay);

        return Result.success();
    }
}
