package com.adictic.client.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.adictic.common.entity.EventBlock;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Dao
public abstract class EventBlockDao {
    @Query("SELECT * FROM EventBlock")
    public abstract Single<List<EventBlock>> getAll();

    @Insert
    public abstract Completable insertAll(List<EventBlock> list);

    @Query("DELETE FROM EventBlock")
    public abstract Completable deleteAll();

    @Query("SELECT * FROM EventBlock WHERE days LIKE '%' || :dia || '%'")
    public abstract Single<List<EventBlock>> getEventsByDay(int dia);

    public Completable update(List<EventBlock> list) {
        return deleteAll()
                .andThen(insertAll(list))
                .subscribeOn(Schedulers.io());
    }
}
