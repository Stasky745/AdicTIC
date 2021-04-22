package com.example.adictic.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FreeUseAppDAO {
    @Query("SELECT * FROM FreeUseApp")
    List<FreeUseApp> getAll();

    @Query("SELECT * FROM FreeUseApp WHERE pkgName LIKE :name")
    FreeUseApp findByPkgName(String name);

    @Update
    void update(FreeUseApp freeUseApp);

    @Delete
    void delete(FreeUseApp freeUseApp);

    @Query("DELETE FROM FreeUseApp")
    void deleteAll();

    @Insert
    void insert(FreeUseApp freeUseApp);
}
