package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.dao.HorarisNitDao;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.util.Converters;

@Database(
        entities = {
                BlockedApp.class,
                EventBlock.class,
                HorarisNit.class
        },
        version = 2
)
@TypeConverters({Converters.class})
public abstract class AdicticDB extends RoomDatabase {
    public abstract BlockedAppDao blockedAppDao();
    public abstract EventBlockDao eventBlockDao();
    public abstract HorarisNitDao horarisNitDao();
}
