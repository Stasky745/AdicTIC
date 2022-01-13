package com.adictic.common.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.HorarisNit;

import java.util.List;

@Dao
public abstract class HorarisNitDao {
    @Query("SELECT * FROM HorarisNit")
    public abstract List<HorarisNit> getAll();

    @Insert
    public abstract void insertAll(List<HorarisNit> list);

    @Query("DELETE FROM HorarisNit")
    public abstract void deleteAll();

    @Query("SELECT * FROM HorarisNit WHERE dia = :dia")
    public abstract HorarisNit findByDay(int dia);

    @Transaction
    public void update(List<HorarisNit> list) {
        deleteAll();
        insertAll(list);
    }
}
