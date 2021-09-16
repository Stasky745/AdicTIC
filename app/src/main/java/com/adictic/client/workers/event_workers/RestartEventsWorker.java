package com.adictic.client.workers.event_workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.util.Funcions;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.util.Constants;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class RestartEventsWorker extends Worker {
    private final static String TAG = "RestartEventsWorker";
    public RestartEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"Worker començat");

        boolean blockedDevice = false;

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

        // Aturem tots els workers d'Events que estiguin configurats
        WorkManager.getInstance(getApplicationContext())
                .cancelAllWorkByTag(Constants.WORKER_TAG_EVENT_BLOCK);

        // Posem les dades dels events a 0
        sharedPreferences.edit().putInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,0).apply();

        // Agafem els events del dia actual
        List<EventBlock> eventsList = Funcions.readFromFile(getApplicationContext(),Constants.FILE_EVENT_BLOCK,false);

        // Si no hi ha events no fem res
        if(eventsList == null || eventsList.isEmpty()) {
            Log.d(TAG,"EventsList i horarisList null/empty -> SUCCESS");
            return Result.success();
        }

        // Si existeixen EVENTS
        Log.d(TAG,"EventsList conté events");
        int now = DateTime.now().getMillisOfDay();

        // Per cada event, mirem quant de temps falta perquè comenci i fem un worker a aquella hora
        for (EventBlock eventBlock : eventsList) {
            if (esDelDia(eventBlock)) {
                Log.d(TAG,"Event " + eventBlock.name + " és del dia actual");

                // Preparem el Worker d'inici d'event
                long delayStart = eventBlock.startEvent - now;

                // Preparem el Worker de fi d'event
                long delayEnd = eventBlock.endEvent - now;

                Log.d(TAG,"Event " + eventBlock.name + " | delayStart=" + delayStart + " | delayEnd=" + delayEnd);

                // Si l'event ja està passant
                if(delayStart < 0 && delayEnd > 0){
                    Log.d(TAG,"Event " + eventBlock.name + " ja està passant");
                    int currentBlockedEvents = sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,0);
                    sharedPreferences.edit().putInt(Constants.SHARED_PREFS_ACTIVE_EVENTS,currentBlockedEvents+1).apply();
                    Funcions.runFinishBlockEventWorker(getApplicationContext(), eventBlock.id, delayEnd);

                    // Bloquegem el dispositiu si no ho està
                    if(!blockedDevice){
                        blockedDevice = true;
//                        DevicePolicyManager mDPM = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
//                        assert mDPM != null;
//                        mDPM.lockNow();
                        Funcions.showBlockDeviceScreen(getApplicationContext());
                    }
                }
                // Si l'event encara ha de començar
                else if(delayStart > 0) {
                    Log.d(TAG,"Event " + eventBlock.name + " ha de començar");
                    Funcions.runStartBlockEventWorker(getApplicationContext(), eventBlock.id, delayStart);
                    Funcions.runFinishBlockEventWorker(getApplicationContext(), eventBlock.id, delayEnd);
                }
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
        else
            return eventBlock.saturday;
    }
}
