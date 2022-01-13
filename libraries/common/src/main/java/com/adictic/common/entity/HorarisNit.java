package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HorarisNit implements Parcelable {
    @PrimaryKey
    public Integer dia;

    // Joda-time getMillisOfDay()
    @ColumnInfo(name = "dormir")
    public Integer dormir;

    @ColumnInfo(name = "despertar")
    public Integer despertar;

    public HorarisNit() { }
    protected HorarisNit(Parcel in) {
        if (in.readByte() == 0) {
            dia = null;
        } else {
            dia = in.readInt();
        }
        if (in.readByte() == 0) {
            dormir = null;
        } else {
            dormir = in.readInt();
        }
        if (in.readByte() == 0) {
            despertar = null;
        } else {
            despertar = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dia == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(dia);
        }
        if (dormir == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(dormir);
        }
        if (despertar == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(despertar);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HorarisNit> CREATOR = new Creator<HorarisNit>() {
        @Override
        public HorarisNit createFromParcel(Parcel in) {
            return new HorarisNit(in);
        }

        @Override
        public HorarisNit[] newArray(int size) {
            return new HorarisNit[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Integer)
            return obj.equals(this.dia);
        if(obj instanceof HorarisNit)
            return this.dia.equals(((HorarisNit) obj).dia);
        else
            return false;
    }
}
