package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
public class EventBlock implements Comparator<Object>, Parcelable {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    // Joda-time getMillisOfDay()
    @ColumnInfo(name = "startEvent")
    public int startEvent;
    @ColumnInfo(name = "endEvent")
    public int endEvent;

    @Ignore
    public boolean activeNow;

    @ColumnInfo(name = "days")
    public List<Integer> days;

    public EventBlock(EventBlock other){
        this.id = other.id;
        this.name = other.name;

        this.startEvent = other.startEvent;
        this.endEvent = other.endEvent;

        this.activeNow = other.activeNow;

        this.days = other.days;
    }

    protected EventBlock(Parcel in) {
        id = in.readLong();
        name = in.readString();
        startEvent = in.readInt();
        endEvent = in.readInt();
        activeNow = in.readByte() != 0;
        in.readList(days, Integer.class.getClassLoader());
    }

    public static final Creator<EventBlock> CREATOR = new Creator<EventBlock>() {
        @Override
        public EventBlock createFromParcel(Parcel in) {
            return new EventBlock(in);
        }

        @Override
        public EventBlock[] newArray(int size) {
            return new EventBlock[size];
        }
    };

    @Override
    public int compare(Object o, Object t1) {
        EventBlock eventBlock1 = (EventBlock) o;
        EventBlock eventBlock2 = (EventBlock) t1;

        return Integer.compare(eventBlock1.startEvent, eventBlock2.startEvent);
    }

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
                this.activeNow == object.activeNow &&
                this.name.equals(object.name) &&
                this.startEvent == object.startEvent &&
                this.endEvent == object.endEvent &&
                this.days.equals(object.days);
    }

    public EventBlock(){
        id = 0;
        name = "";
        startEvent = 0;
        endEvent = 0;
        days = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeInt(startEvent);
        parcel.writeInt(endEvent);
        parcel.writeByte((byte) (activeNow ? 1 : 0));
        parcel.writeList(days);
    }
}
