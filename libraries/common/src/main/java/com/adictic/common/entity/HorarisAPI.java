package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

public class HorarisAPI implements Parcelable {
    public Integer tipus;
    public Collection<HorarisNit> horarisNit;
    public Boolean actiu;

    public HorarisAPI() { }
    protected HorarisAPI(Parcel in) {
        if (in.readByte() == 0) {
            tipus = null;
        } else {
            tipus = in.readInt();
        }
        byte tmpActiu = in.readByte();
        actiu = tmpActiu == 0 ? null : tmpActiu == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (tipus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(tipus);
        }
        dest.writeByte((byte) (actiu == null ? 0 : actiu ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HorarisAPI> CREATOR = new Creator<HorarisAPI>() {
        @Override
        public HorarisAPI createFromParcel(Parcel in) {
            return new HorarisAPI(in);
        }

        @Override
        public HorarisAPI[] newArray(int size) {
            return new HorarisAPI[size];
        }
    };
}
