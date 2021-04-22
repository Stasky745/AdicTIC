package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.roomdb.EventBlock;
import com.example.adictic.roomdb.FreeUseApp;
import com.example.adictic.roomdb.RoomRepo;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FetchEventsWorker extends Worker {
    RoomRepo roomRepo;

    public FetchEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        roomRepo = new RoomRepo(getApplicationContext());

        // Agafem els events del dia actual
        List<EventBlock> eventsDia = agafarEventsDia();

        // Si no hi ha events no fem res
        if(eventsDia == null) return Result.success();

        // Per cada event, mirem quant de temps falta perquè comenci i fem un worker a aquella hora
        for(EventBlock eventBlock : eventsDia){
            DateTime dateTime = new DateTime()
                    .withMillisOfDay(eventBlock.startEvent);

            long delay = dateTime.getMillis() - DateTime.now().getMillis();

            Funcions.runStartBlockEventWorker(getApplicationContext(),eventBlock.name,delay);
        }

        // Agafem la taula de FreeUseApps
        List<FreeUseApp> freeUseApps = roomRepo.getAllFreeUseApps();

        // Per cada FreeUseApp, inicialitzem l'us inicial a 0 perquè acaba de començar un dia nou
        for(FreeUseApp app : freeUseApps){
            app.millisUsageStart = 0;
            roomRepo.updateFreeUseApp(app);
        }

        return Result.success();
    }

    private List<EventBlock> agafarEventsDia(){
        // 1 - diumenge --> 7 - dissabte
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

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
