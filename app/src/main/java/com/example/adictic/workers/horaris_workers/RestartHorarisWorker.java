package com.example.adictic.workers.horaris_workers;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.common.util.Constants;
import com.adictic.common.entity.HorarisNit;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class RestartHorarisWorker extends Worker {
    private final String TAG = "RestartHorarisWorker";

    public RestartHorarisWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"Worker començat");

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences != null;

        // Aturem tots els workers d'Horaris que estiguin configurats
        WorkManager.getInstance(getApplicationContext())
                .cancelAllWorkByTag(Constants.WORKER_TAG_HORARIS_BLOCK);

        // Desactivem els horaris si estan actius
        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT, false).apply();

        // Agafem la llista d'horaris
        List<HorarisNit> horarisNitList = Funcions.readFromFile(getApplicationContext(),Constants.FILE_HORARIS_NIT,false);

        int avui = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        assert horarisNitList != null;
        HorarisNit horarisAvui = horarisNitList.stream()
                .filter(horarisNit -> horarisNit.dia == avui)
                .findAny()
                .orElse(null);

        if (horarisAvui == null || horarisAvui.despertar.equals(horarisAvui.dormir)) {
            return Result.success();
        }

        boolean bloquejat = false;
        long now = DateTime.now().getMillisOfDay();

        // Mirem si hem de programar el despertar
        if(horarisAvui.despertar > now){
            bloquejat = true;
            Funcions.runDespertarWorker(getApplicationContext(),horarisAvui.despertar - now);
            if(horarisAvui.dormir != -1)
                Funcions.runDormirWorker(getApplicationContext(), horarisAvui.dormir - now);
        }
        else if(now > horarisAvui.despertar && horarisAvui.dormir != -1)
            Funcions.runDormirWorker(getApplicationContext(), horarisAvui.dormir - now);
        else if(now > horarisAvui.dormir && horarisAvui.dormir != -1)
            bloquejat = true;

        // Si ha d'estar bloquejat ho bloquegem
        if(bloquejat){
            DevicePolicyManager mDPM = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            assert mDPM != null;
            mDPM.lockNow();
        }

        sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_ACTIVE_HORARIS_NIT, bloquejat).apply();

        return Result.success();
    }
}
