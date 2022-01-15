package com.adictic.common.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.EventBlock;

import java.util.List;

@Dao
public abstract class EventBlockDao {
    @Query("SELECT * FROM EventBlock")
    public abstract List<EventBlock> getAll();

    @Insert
    public abstract void insertAll(List<EventBlock> list);

    @Query("DELETE FROM EventBlock")
    public abstract void deleteAll();

    @Query("SELECT * FROM EventBlock WHERE days LIKE :dia")
    public abstract List<EventBlock> getEventsByDay(int dia);

    @Transaction
    public void update(List<EventBlock> list) {
        deleteAll();
        insertAll(list);
    }
}
