package com.example.adictic.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BlockedAppDAO {
    @Query("SELECT * FROM BlockedApp")
    List<BlockedApp> getAll();

    @Query("SELECT * FROM BlockedApp WHERE pkgName LIKE :name")
    BlockedApp findByPkgName(String name);

    @Delete
    void delete(BlockedApp blockedApp);

    @Insert
    void insert(BlockedApp blockedApp);
}
