package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

public class GeneralUsage implements Parcelable {
    public Integer day;
    public Integer month;
    public Integer year;
    public Collection<AppUsage> usage;

    public Long totalTime;

    public GeneralUsage(){ }

    protected GeneralUsage(Parcel in) {
        if (in.readByte() == 0) {
            day = null;
        } else {
            day = in.readInt();
        }
        if (in.readByte() == 0) {
            month = null;
        } else {
            month = in.readInt();
        }
        if (in.readByte() == 0) {
            year = null;
        } else {
            year = in.readInt();
        }
        if (in.readByte() == 0) {
            totalTime = null;
        } else {
            totalTime = in.readLong();
        }
    }

    public static final Creator<GeneralUsage> CREATOR = new Creator<GeneralUsage>() {
        @Override
        public GeneralUsage createFromParcel(Parcel in) {
            return new GeneralUsage(in);
        }

        @Override
        public GeneralUsage[] newArray(int size) {
            return new GeneralUsage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (day == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(day);
        }
        if (month == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(month);
        }
        if (year == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(year);
        }
        if (totalTime == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(totalTime);
        }
    }
}
