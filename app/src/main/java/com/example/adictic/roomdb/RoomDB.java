package com.example.adictic.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = {EventBlock.class, HorarisNit.class, BlockedApp.class})
public abstract class RoomDB extends RoomDatabase {
    public abstract EventBlockDAO eventBlockDAO();
    public abstract HorarisNitDAO horarisNitDAO();
    public abstract BlockedAppDAO blockedAppDAO();
}
