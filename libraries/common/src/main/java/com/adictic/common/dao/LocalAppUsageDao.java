package com.adictic.common.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.adictic.common.entity.LocalAppUsage;

import java.util.List;

@Dao
public abstract class LocalAppUsageDao {
    @Query("SELECT * FROM LocalAppUsage")
    public abstract List<LocalAppUsage> getAll();

    @Insert
    public abstract void insertAll(List<LocalAppUsage> list);

    @Delete
    public abstract void delete(LocalAppUsage localAppUsage);

    @Query("DELETE FROM LocalAppUsage")
    public abstract void deleteAll();

    @Query("SELECT * FROM LocalAppUsage WHERE pkgName LIKE :pkgName")
    public abstract LocalAppUsage findByName(String pkgName);

    @Transaction
    public void update(List<LocalAppUsage> list) {
        deleteAll();
        insertAll(list);
    }
}
