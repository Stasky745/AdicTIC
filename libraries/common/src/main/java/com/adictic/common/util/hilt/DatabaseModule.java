package com.adictic.common.util.hilt;

import android.app.Application;

import androidx.room.Room;

import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.dao.HorarisNitDao;
import com.adictic.common.database.AdicticDB;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static AdicticDB provideAdicticDB(@ApplicationContext Application application) {
        return Room.databaseBuilder(
                application,
                AdicticDB.class,
                Constants.ROOM_ADICTIC_DATABASE)
                .enableMultiInstanceInvalidation()
                .build();
    }

    @Provides
    @Singleton
    public static BlockedAppDao provideAppDao(AdicticDB adicticDB) {
        return adicticDB.blockedAppDao();
    }

    @Provides
    @Singleton
    public static HorarisNitDao provideHorarisDao(AdicticDB adicticDB) {
        return adicticDB.horarisNitDao();
    }

    @Provides
    @Singleton
    public static EventBlockDao provideEventDao(AdicticDB adicticDB) {
        return adicticDB.eventBlockDao();
    }
}
