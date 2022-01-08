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

    @Query("DELETE FROM BlockedApp")
    public abstract void deleteAll();

    @Query("SELECT * FROM BlockedApp WHERE pkgName LIKE :pkgName")
    public abstract BlockedApp findByName(String pkgName);

    @Query("SELECT pkgName FROM BlockedApp JOIN LocalAppUsage ON pkgName = LocalAppUsage.pkgName WHERE timeLimit <= 0 OR timeLimit <= LocalAppUsage.totalTime")
    public abstract List<String> getAllBlockedApps();

    @Query("SELECT * FROM BlockedApp JOIN LocalAppUsage ON pkgName = LocalAppUsage.pkgName WHERE pkgName LIKE :pkgName AND (timeLimit <= 0 OR timeLimit <= LocalAppUsage.totalTime)")
    public abstract BlockedApp isAppBlocked(String pkgName);

    @Query("SELECT pkgName FROM BlockedApp JOIN LocalAppUsage ON pkgName = LocalAppUsage.pkgName WHERE timeLimit <= 0 OR timeLimit <= LocalAppUsage.totalTime")
    public abstract List<String> getPermanentBlockedApps();

    @Transaction
    public void update(List<BlockedApp> list) {
        deleteAll();
        insertAll(list);
    }
}
