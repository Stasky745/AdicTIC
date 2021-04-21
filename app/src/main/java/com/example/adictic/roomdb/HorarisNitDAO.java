package com.example.adictic.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HorarisNitDAO {
    @Query("SELECT * FROM HorarisNit")
    List<HorarisNit> getAll();

    @Query("SELECT * FROM HorarisNit WHERE idDia = :id")
    HorarisNit findByDay(long id);

    @Delete
    void delete(HorarisNit horarisNit);

    @Insert
    void insert(HorarisNit horarisNit);

    @Update
    void update(HorarisNit horarisNit);
}
