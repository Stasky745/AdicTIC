package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.adictic.common.dao.LocalEventBlockDao;
import com.adictic.common.entity.LocalEventBlock;
import com.adictic.common.util.Converters;

@Database(
        entities = {
                LocalEventBlock.class
        },
        version = 1
)
@TypeConverters({ Converters.class })
public abstract class EventsDatabase {
    public abstract LocalEventBlockDao localEventBlockDao();
}
