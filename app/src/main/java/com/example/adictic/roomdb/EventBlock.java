package com.example.adictic.roomdb;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.joda.time.DateTime;

import java.util.Date;

@Entity(indices = {@Index(value = {"nom_event"}, unique = true)})
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

    @Override
    public boolean equals(@Nullable Object obj) {
//        return super.equals(obj);

        if(obj instanceof EventBlock){
            EventBlock object = (EventBlock) obj;
            return this.id == object.id;
        }
        else return false;
    }

    public boolean exactSame(EventBlock object){
        return this.id == object.id &&
                this.monday == object.monday &&
                this.tuesday == object.tuesday &&
                this.wednesday == object.wednesday &&
                this.thursday == object.thursday &&
                this.friday == object.friday &&
                this.saturday == object.saturday &&
                this.sunday == object.sunday &&
                this.activeNow == object.activeNow &&
                this.name.equals(object.name) &&
                this.startEvent == object.startEvent &&
                this.endEvent == object.endEvent;
    }
}
