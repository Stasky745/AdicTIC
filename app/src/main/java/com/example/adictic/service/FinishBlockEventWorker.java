package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.EventBlock;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

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
        long id = getInputData().getLong("id",-1);

        // Agafem l'event del repositori
        List<EventBlock> list = Funcions.readFromFile(getApplicationContext(), Constants.FILE_EVENT_BLOCK,false);
        EventBlock eventBlock = list.get(list.indexOf(id));
        eventBlock.activeNow = false;
        Funcions.write2File(getApplicationContext(),list);

//        HorarisEvents event = Funcions.getEventFromList(name);
//        List<Integer> listDays = event.days;
//        Collections.sort(listDays);
//        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//        int dayJump;



//        /* Si el següent dia és a la propera setmana **/
//        if (currentDay == listDays.get(listDays.size() - 1))
//            dayJump = 7 - (currentDay - listDays.get(0));
//            /* Si el següent dia és de la mateixa setmana **/
//        else {
//            int nextDay = listDays.get(listDays.indexOf(currentDay) + 1);
//            dayJump = nextDay - currentDay;
//        }
//
//        String eventStart = event.start;
//        Pair<Integer, Integer> timeStart = Funcions.stringToTime(eventStart);
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, dayJump);
//        cal.set(Calendar.HOUR_OF_DAY, timeStart.first);
//        cal.set(Calendar.MINUTE, timeStart.second);
//
//        long delay = cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
//
//        TodoApp.removeBlockEvent(name);
//
//        Funcions.runStartBlockEventWorker(getApplicationContext(), name, delay);

        return Result.success();
    }
}
