package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.entity.BlockedApp;

@Database(
        entities = {
                BlockedApp.class
        },
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BlockedAppDao blockedAppDao();
}
