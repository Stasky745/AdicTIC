package com.example.adictic.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.EventBlock;
import com.example.adictic.entity.FreeUseApp;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class FetchEventsWorker extends Worker {
    public FetchEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Agafem els events del dia actual
        List<EventBlock> eventsList = Funcions.readFromFile(getApplicationContext(),Constants.FILE_EVENT_BLOCK,false);

        // Si no hi ha events no fem res
        if(eventsList == null || eventsList.isEmpty()) return Result.success();

        // Per cada event, mirem quant de temps falta perquè comenci i fem un worker a aquella hora
        for(EventBlock eventBlock : eventsList){
            if(esDelDia(eventBlock)) {
                DateTime dateTime = new DateTime()
                        .withMillisOfDay(eventBlock.startEvent);

                long delay = dateTime.getMillis() - DateTime.now().getMillis();

                Funcions.runStartBlockEventWorker(getApplicationContext(), eventBlock.id, delay);
            }
        }

        // Agafem la taula de FreeUseApps
        List<FreeUseApp> freeUseApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_FREE_USE_APPS,false);

        // Per cada FreeUseApp, inicialitzem l'us inicial a 0 perquè acaba de començar un dia nou
        freeUseApps.forEach(freeUseApp -> freeUseApp.millisUsageStart = 0);
        Funcions.write2File(getApplicationContext(),freeUseApps);

        return Result.success();
    }

    private boolean esDelDia(EventBlock eventBlock){
        // 1 - diumenge --> 7 - dissabte
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        if(dia == 1){
            return eventBlock.sunday;
        }
        else if(dia == 2){
            return eventBlock.monday;
        }
        else if(dia == 3){
            return eventBlock.tuesday;
        }
        else if(dia == 4){
            return eventBlock.wednesday;
        }
        else if(dia == 5){
            return eventBlock.thursday;
        }
        else if(dia == 6){
            return eventBlock.friday;
        }
        else return eventBlock.saturday;
    }
}
