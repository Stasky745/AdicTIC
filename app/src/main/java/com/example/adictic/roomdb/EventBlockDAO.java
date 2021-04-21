package com.example.adictic.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventBlockDAO {
    @Query("SELECT * FROM EventBlock")
    List<EventBlock> getAll();

    @Query("SELECT * FROM EventBlock WHERE nom_event LIKE :nom")
    EventBlock getEventFromList(String nom);

    @Query("SELECT * FROM EventBlock WHERE activeNow")
    List<EventBlock> getActiveEvents();

    @Query("SELECT * FROM EventBlock WHERE monday")
    List<EventBlock> getMonday();
    @Query("SELECT * FROM EventBlock WHERE tuesday")
    List<EventBlock> getTuesday();
    @Query("SELECT * FROM EventBlock WHERE wednesday")
    List<EventBlock> getWednesday();
    @Query("SELECT * FROM EventBlock WHERE thursday")
    List<EventBlock> getThursday();
    @Query("SELECT * FROM EventBlock WHERE friday")
    List<EventBlock> getFriday();
    @Query("SELECT * FROM EventBlock WHERE saturday")
    List<EventBlock> getSaturday();
    @Query("SELECT * FROM EventBlock WHERE sunday")
    List<EventBlock> getSunday();

    @Update
    void update(EventBlock eventBlock);

    @Delete
    void delete(EventBlock eventBlock);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventBlock eventBlock);

}
