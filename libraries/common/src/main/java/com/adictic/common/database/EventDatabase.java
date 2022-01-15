package com.adictic.common.database;

import androidx.room.Database;
import androidx.room.TypeConverters;

import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.util.Converters;

@Database(
        entities = {
                EventBlock.class
        },
        version = 1
)
@TypeConverters({Converters.class})
public abstract class EventDatabase {
    public abstract EventBlockDao eventBlockDao();
}
