package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class User implements Parcelable {
    public long id;
    public Long adminId;
    public Integer tutor;
    public List<FillNom> llista;
    public String name; //S'ha de treure despres de canviar lo de ChatInfo
    public boolean temporalPass;

    protected User(Parcel in) {
        id = in.readLong();
        if (in.readByte() == 0) {
            adminId = null;
        } else {
            adminId = in.readLong();
        }
        if (in.readByte() == 0) {
            tutor = null;
        } else {
            tutor = in.readInt();
        }
        llista = in.createTypedArrayList(FillNom.CREATOR);
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        if (adminId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(adminId);
        }
        if (tutor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(tutor);
        }
        dest.writeTypedList(llista);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
