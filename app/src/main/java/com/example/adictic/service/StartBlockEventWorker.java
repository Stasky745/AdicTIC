package com.example.adictic.service;

import android.content.Context;

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

    @NonNull
    @Override
    public Result doWork() {
        long id = getInputData().getLong("id",-1);

        // Agafem l'event del repositori
        List<EventBlock> list = Funcions.readFromFile(getApplicationContext(), Constants.FILE_EVENT_BLOCK,false);
        EventBlock eventBlock = list.get(list.indexOf(id));
        eventBlock.activeNow = true;
        int endTime = eventBlock.endEvent;

        //Ens guardem l'hora a què acaba l'event
        DateTime dateTime = new DateTime()
                .withMillisOfDay(endTime);

        Funcions.write2File(getApplicationContext(),list);

//        HorarisEvents event = Funcions.getEventFromList(name);
//        String eventEnd = event.finish;
//        Pair<Integer, Integer> timeFinish = Funcions.stringToTime(eventEnd);
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, timeFinish.first);
//        cal.set(Calendar.MINUTE, timeFinish.second);

        long delay = dateTime.getMillis() - Calendar.getInstance().getTimeInMillis();

        //TodoApp.addBlockEvent(name);

        Funcions.runFinishBlockEventWorker(getApplicationContext(), id, delay);

        return Result.success();
    }
}
