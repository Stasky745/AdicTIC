package com.adictic.common.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.BlockedApp;

import java.util.List;

@Dao
public abstract class BlockedAppDao {
    @Query("SELECT * FROM BlockedApp")
    public abstract List<BlockedApp> getAll();

    @Insert
    public abstract void insertAll(List<BlockedApp> list);

    @Delete
    public abstract void delete(BlockedApp blockedApp);

    @Query("DELETE FROM BlockedApp WHERE BlockedApp.pkgName LIKE :pkgName")
    public abstract void deleteByName(String pkgName);

    @Query("DELETE FROM BlockedApp")
    public abstract void deleteAll();

    @Transaction
    public void update(List<BlockedApp> list) {
        deleteAll();
        insertAll(list);
    }
}
