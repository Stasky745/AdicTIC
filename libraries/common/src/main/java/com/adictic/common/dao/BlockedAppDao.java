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

    @Query("SELECT BlockedApp.pkgName FROM BlockedApp JOIN LocalAppUsage ON BlockedApp.pkgName = LocalAppUsage.pkgName WHERE BlockedApp.timeLimit <= 0 OR BlockedApp.timeLimit <= LocalAppUsage.totalTime")
    public abstract List<String> getAllBlockedApps();

    @Query("SELECT BlockedApp.* FROM BlockedApp JOIN LocalAppUsage ON BlockedApp.pkgName = LocalAppUsage.pkgName WHERE BlockedApp.pkgName LIKE :pkgName AND (BlockedApp.timeLimit <= 0 OR BlockedApp.timeLimit <= LocalAppUsage.totalTime)")
    public abstract BlockedApp isAppBlocked(String pkgName);

    @Query("SELECT BlockedApp.pkgName FROM BlockedApp JOIN LocalAppUsage ON BlockedApp.pkgName = LocalAppUsage.pkgName WHERE BlockedApp.timeLimit <= 0 OR BlockedApp.timeLimit <= LocalAppUsage.totalTime")
    public abstract List<String> getPermanentBlockedApps();

    @Transaction
    public void update(List<BlockedApp> list) {
        deleteAll();
        insertAll(list);
    }
}
