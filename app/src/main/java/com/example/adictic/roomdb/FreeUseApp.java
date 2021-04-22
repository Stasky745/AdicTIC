package com.example.adictic.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FreeUseApp {
    @NonNull
    @PrimaryKey
    public String pkgName = "";

    public long millisUsageStart;
    public long millisUsageEnd;
}
