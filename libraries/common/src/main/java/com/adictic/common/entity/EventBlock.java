package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.Comparator;

public class EventBlock implements Comparator<Object>, Parcelable {
    public long id;

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

    public EventBlock(EventBlock other){
        this.id = other.id;
        this.name = other.name;

        this.startEvent = other.startEvent;
        this.endEvent = other.endEvent;

        this.activeNow = other.activeNow;

        this.monday = other.monday;
        this.tuesday = other.tuesday;
        this.wednesday = other.wednesday;
        this.thursday = other.thursday;
        this.friday = other.friday;
        this.saturday = other.saturday;
        this.sunday = other.sunday;
    }

    protected EventBlock(Parcel in) {
        id = in.readLong();
        name = in.readString();
        startEvent = in.readInt();
        endEvent = in.readInt();
        activeNow = in.readByte() != 0;
        monday = in.readByte() != 0;
        tuesday = in.readByte() != 0;
        wednesday = in.readByte() != 0;
        thursday = in.readByte() != 0;
        friday = in.readByte() != 0;
        saturday = in.readByte() != 0;
        sunday = in.readByte() != 0;
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

    public EventBlock(){
        id = 0;
        name = "";
        startEvent = 0;
        endEvent = 0;

        monday = false;
        tuesday = false;
        wednesday = false;
        thursday = false;
        friday = false;
        saturday = false;
        sunday = false;
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
        parcel.writeByte((byte) (monday ? 1 : 0));
        parcel.writeByte((byte) (tuesday ? 1 : 0));
        parcel.writeByte((byte) (wednesday ? 1 : 0));
        parcel.writeByte((byte) (thursday ? 1 : 0));
        parcel.writeByte((byte) (friday ? 1 : 0));
        parcel.writeByte((byte) (saturday ? 1 : 0));
        parcel.writeByte((byte) (sunday ? 1 : 0));
    }
}
