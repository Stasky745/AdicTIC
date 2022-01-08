package com.adictic.common.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BlockedApp {
    @PrimaryKey
    public String pkgName;

    // -1 si bloqueig permanent
    @ColumnInfo(name = "timeLimit")
    public long timeLimit;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return obj.equals(this.pkgName);
        if(obj instanceof BlockedApp) return ((BlockedApp) obj).pkgName.equals(this.pkgName);
        else return false;
    }
}
