package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.LocalAppUsageDao;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.LocalAppUsage;

@Database(
        entities = {
                BlockedApp.class,
                LocalAppUsage.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BlockedAppDao blockedAppDao();
    public abstract LocalAppUsageDao localAppUsageDao();
}
