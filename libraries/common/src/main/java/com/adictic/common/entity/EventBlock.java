package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventBlock implements Comparator<Object>, Parcelable {
    public long id;

    public String name;

    // Joda-time getMillisOfDay()
    public int startEvent;
    public int endEvent;

    public List<Integer> days;

    public EventBlock(EventBlock other){
        this.id = other.id;
        this.name = other.name;

        this.startEvent = other.startEvent;
        this.endEvent = other.endEvent;

        this.days = other.days;
    }

    protected EventBlock(Parcel in) {
        id = in.readLong();
        name = in.readString();
        startEvent = in.readInt();
        endEvent = in.readInt();
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
        parcel.writeList(days);
    }
}
