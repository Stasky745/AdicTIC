package com.adictic.common.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocalAppUsage {
    @PrimaryKey
    @NonNull
    public String pkgName;

    @ColumnInfo(name = "totalTime")
    public Long totalTime;
}
