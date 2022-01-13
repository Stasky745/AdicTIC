package com.adictic.common.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class LocalEventBlock {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "name")
    public String Name;

    @ColumnInfo(name = "startEvent")
    public int startEvent;

    @ColumnInfo(name = "endEvent")
    public int endEvent;

    @ColumnInfo(name = "days")
    public List<Integer> days;
}
