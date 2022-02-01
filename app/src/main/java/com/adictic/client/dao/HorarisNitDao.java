package com.adictic.client.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.HorarisNit;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class HorarisNitDao {
    @Query("SELECT * FROM HorarisNit")
    public abstract Single<List<HorarisNit>> getAll();

    @Insert
    public abstract Completable insertAll(List<HorarisNit> list);

    @Query("DELETE FROM HorarisNit")
    public abstract Completable deleteAll();

    @Query("SELECT * FROM HorarisNit WHERE dia = :dia")
    public abstract Single<HorarisNit> findByDay(int dia);

    @Transaction
    public void update(List<HorarisNit> list) {
        deleteAll();
        insertAll(list);
    }
}
