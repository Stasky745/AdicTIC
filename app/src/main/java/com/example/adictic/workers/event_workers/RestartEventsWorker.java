package com.example.adictic.workers.event_workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.entity.EventBlock;
import com.example.adictic.entity.FreeUseApp;
import com.example.adictic.entity.HorarisNit;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RestartEventsWorker extends Worker {
    public RestartEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        // Aturem tots els workers d'Events que estiguin configurats
        WorkManager.getInstance(getApplicationContext())
                .cancelAllWorkByTag(Constants.WORKER_TAG_EVENT_BLOCK);

        // Agafem els events del dia actual
        List<EventBlock> eventsList = Funcions.readFromFile(getApplicationContext(),Constants.FILE_EVENT_BLOCK,false);
        List<HorarisNit> horarisNitList = Funcions.readFromFile(getApplicationContext(),Constants.FILE_HORARIS_NIT,false);

        // Si no hi ha events no fem res
        if((eventsList == null || eventsList.isEmpty()) && (horarisNitList == null || horarisNitList.isEmpty()))
            return Result.success();

        // Si existeixen EVENTS
        if(eventsList != null && !eventsList.isEmpty()){
            // Per cada event, mirem quant de temps falta perquè comenci i fem un worker a aquella hora
            for (EventBlock eventBlock : eventsList) {
                if (esDelDia(eventBlock)) {
                    // Preparem el Worker d'inici d'event
                    DateTime startEvent = new DateTime()
                            .withMillisOfDay(eventBlock.startEvent);

                    long delayStart = startEvent.getMillis() - DateTime.now().getMillisOfDay();

                    // Preparem el Worker de fi d'event
                    DateTime finishEvent = new DateTime()
                            .withMillisOfDay(eventBlock.endEvent);

                    long delayEnd = finishEvent.getMillis() - DateTime.now().getMillisOfDay();

                    // Si l'event ja està passant
                    if(delayStart < 0 && delayEnd > 0){
                        assert sharedPreferences != null;
                        int currentBlockedEvents = sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,0);
                        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,currentBlockedEvents+1).apply();
                        Funcions.runFinishBlockEventWorker(getApplicationContext(), eventBlock.id, delayEnd);
                    }
                    // Si l'event encara ha de començar
                    else if(delayStart > 0) {
                        Funcions.runStartBlockEventWorker(getApplicationContext(), eventBlock.id, delayStart);
                        Funcions.runFinishBlockEventWorker(getApplicationContext(), eventBlock.id, delayEnd);
                    }
                }
            }

            // Agafem la taula de FreeUseApps
            List<FreeUseApp> freeUseApps = Funcions.readFromFile(getApplicationContext(), Constants.FILE_FREE_USE_APPS, false);

            // Per cada FreeUseApp, inicialitzem l'us inicial a 0 perquè acaba de començar un dia nou
            if(freeUseApps == null)
                freeUseApps = new ArrayList<>();

            freeUseApps.forEach(freeUseApp -> freeUseApp.millisUsageStart = 0);
            Funcions.write2File(getApplicationContext(), freeUseApps);
        }

        // Si existeixen HORARISNIT
        if(horarisNitList != null && !horarisNitList.isEmpty()){
            HorarisNit avui = horarisNitList.stream().filter(horarisNit -> horarisNit.idDia.equals(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))).findAny().get();
            long now = DateTime.now().getMillisOfDay();

            if(now < avui.despertar){
                Funcions.runDespertarWorker(getApplicationContext(),avui.despertar - now);
                Funcions.runDormirWorker(getApplicationContext(), avui.dormir - now);
            }
            else if(now < avui.dormir){
                Funcions.runDormirWorker(getApplicationContext(), avui.dormir - now);
            }
            else {
                assert sharedPreferences != null;
                sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT,true).apply();
            }
        }

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
