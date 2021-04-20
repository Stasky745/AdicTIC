package com.example.adictic.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BlockedApp {
    @NonNull
    @PrimaryKey
    public String pkgName;

    // -1 si bloqueig permanent
    public long timeLimit;
}
