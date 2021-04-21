package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.roomdb.EventBlock;
import com.example.adictic.roomdb.RoomRepo;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FetchEventsWorker extends Worker {
    public FetchEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<EventBlock> eventsDia = agafarEventsDia();

        if(eventsDia == null) return Result.success();

        for(EventBlock eventBlock : eventsDia){
            DateTime dateTime = new DateTime()
                    .withMillisOfDay(eventBlock.startEvent);

            long delay = dateTime.getMillis() - DateTime.now().getMillis();

            Funcions.runStartBlockEventWorker(getApplicationContext(),eventBlock.name,delay);
        }

        return Result.success();
    }

    private List<EventBlock> agafarEventsDia(){
        // 1 - diumenge --> 7 - dissabte
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        RoomRepo roomRepo = new RoomRepo(getApplicationContext());

        if(dia == 1){
            return roomRepo.getSundayEvents();
        }
        else if(dia == 2){
            return roomRepo.getMondayEvents();
        }
        else if(dia == 3){
            return roomRepo.getTuesdayEvents();
        }
        else if(dia == 4){
            return roomRepo.getWednesdayEvents();
        }
        else if(dia == 5){
            return roomRepo.getThursdayEvents();
        }
        else if(dia == 6){
            return roomRepo.getFridayEvents();
        }
        else return roomRepo.getSaturdayEvents();
    }
}
