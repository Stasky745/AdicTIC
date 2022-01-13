package com.adictic.common.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.LocalEventBlock;

import java.util.List;

@Dao
public abstract class LocalEventBlockDao {
    @Query("SELECT * FROM LocalEventBlock")
    public abstract List<LocalEventBlock> getAll();

    @Insert
    public abstract void insertAll(List<LocalEventBlock> list);

    @Query("DELETE FROM LocalEventBlock")
    public abstract void deleteAll();

    @Query("SELECT * FROM LocalEventBlock WHERE :dia = ANY(days)")
    public abstract LocalEventBlock findByDay(int dia);

    @Transaction
    public void update(List<LocalEventBlock> list) {
        deleteAll();
        insertAll(list);
    }
}
