package com.example.adictic.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BlockedApp {
    @PrimaryKey
    public String pkgName;

    // -1 si bloqueig permanent
    public long timeLimit;
}
