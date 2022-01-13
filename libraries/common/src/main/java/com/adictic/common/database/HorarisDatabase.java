package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.adictic.common.dao.HorarisNitDao;
import com.adictic.common.entity.HorarisNit;

@Database(
        entities = {
                HorarisNit.class
        },
        version = 1
)
public abstract class HorarisDatabase extends RoomDatabase {
    public abstract HorarisNitDao horarisNitDao();
}
