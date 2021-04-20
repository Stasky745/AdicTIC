package com.example.adictic.roomdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.joda.time.DateTime;

import java.util.Date;

@Entity
public class EventBlock {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "nom_event")
    public String name;

    // Joda-time getMillisOfDay()
    public int startEvent;
    public int endEvent;

    public boolean activeNow;

    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;
    public boolean sunday;
}
