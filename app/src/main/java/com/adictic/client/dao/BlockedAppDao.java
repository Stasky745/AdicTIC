package com.adictic.client.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.adictic.common.entity.BlockedApp;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Dao
public abstract class BlockedAppDao {
    @Query("SELECT * FROM BlockedApp")
    public abstract Single<List<BlockedApp>> getAll();

    @Insert
    public abstract Completable insertAll(List<BlockedApp> list);

    @Delete
    public abstract Completable delete(BlockedApp blockedApp);

    @Query("DELETE FROM BlockedApp WHERE BlockedApp.pkgName LIKE :pkgName")
    public abstract Completable deleteByName(String pkgName);

    @Query("DELETE FROM BlockedApp")
    public abstract Completable deleteAll();

    public Completable update(List<BlockedApp> list) {
        return deleteAll()
                .andThen(insertAll(list))
                .subscribeOn(Schedulers.io());
    }
}
