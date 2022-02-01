package com.adictic.client.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adictic.client.util.hilt.AdicticEntryPoint;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.HorarisNit;

import org.joda.time.DateTime;

import java.util.List;

import dagger.hilt.EntryPoints;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HorarisEventsWorkerManager extends Worker {
    private final static String TAG = "HorarisEventsWorkerManager";

    AdicticRepository repository;

    private CompositeDisposable disposable;

    public HorarisEventsWorkerManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        repository = EntryPoints.get(getApplicationContext(), AdicticEntryPoint.class).getAdicticRepository();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        disposable.dispose();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"Worker començat");

        if(!repository.accessibilityServiceOn())
            return Result.failure();

        long millisAra = DateTime.now().getMillis();
        long millisAvui = DateTime.now().withTimeAtStartOfDay().getMillis();
        long millisDema = DateTime.now().plusDays(1).withTimeAtStartOfDay().getMillis();

        boolean esDiaNou = (millisAra - millisAvui) < (millisDema - millisAvui);

        if(!esDiaNou) {
            return Result.retry();
        }

        // Agafem i apliquem horaris
        Disposable disposableHoraris = repository.getAllHoraris()
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<List<HorarisNit>>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<HorarisNit> horarisNits) {
                        repository.setHoraris(horarisNits);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });

        disposable.add(disposableHoraris);

        // Agafem i apliquem Events
        Disposable disposableEvents = repository.getAllEvents()
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<List<EventBlock>>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<EventBlock> eventBlocks) {
                        repository.setEvents(eventBlocks);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });

        disposable.add(disposableEvents);

        return Result.success();
    }
}
