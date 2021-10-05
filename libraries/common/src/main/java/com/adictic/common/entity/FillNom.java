package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class FillNom implements Parcelable {
    public long idChild;
    public String deviceName;
    public boolean blocked;
    public String birthday;
    public boolean freeuse;
    public Integer dailyLimit;

    public FillNom() { }

    protected FillNom(Parcel in) {
        idChild = in.readLong();
        deviceName = in.readString();
        blocked = in.readByte() != 0;
        birthday = in.readString();
        freeuse = in.readByte() != 0;
        dailyLimit = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(idChild);
        dest.writeString(deviceName);
        dest.writeByte((byte) (blocked ? 1 : 0));
        dest.writeString(birthday);
        dest.writeByte((byte) (freeuse ? 1 : 0));
        if(dailyLimit == null)
            dest.writeInt(0);
        else
            dest.writeInt(dailyLimit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FillNom> CREATOR = new Creator<FillNom>() {
        @Override
        public FillNom createFromParcel(Parcel in) {
            return new FillNom(in);
        }

        @Override
        public FillNom[] newArray(int size) {
            return new FillNom[size];
        }
    };
}
