package com.example.adictic.roomdb;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BlockedAppDAO {
    @Query("SELECT * FROM BlockedApp")
    List<BlockedApp> getAll();

    @Query("SELECT * FROM BlockedApp WHERE NOT blockedNow")
    List<BlockedApp> getAllNotBlocked();

    @Query("SELECT * FROM BlockedApp WHERE pkgName LIKE :name")
    BlockedApp findByPkgName(String name);

    @Update
    void update(BlockedApp blockedApp);

    @Delete
    void delete(BlockedApp blockedApp);

    @Query("DELETE FROM BlockedApp")
    int deleteAll();

    @Insert
    void insert(BlockedApp blockedApp);
}
