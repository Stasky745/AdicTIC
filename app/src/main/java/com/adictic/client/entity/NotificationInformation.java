package com.adictic.client.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationInformation implements Parcelable {
    public String title;
    public String message;
    public Long dateMillis;
    public Boolean read;
    public Boolean important;
    public String childName;
    public String notifCode;

    public NotificationInformation(){
        title = "";
        message = "";
        dateMillis = -1L;
        read = false;
        important = false;
        childName = "";
        notifCode = "";
    }

    protected NotificationInformation(Parcel in) {
        title = in.readString();
        message = in.readString();
        if (in.readByte() == 0) {
            dateMillis = null;
        } else {
            dateMillis = in.readLong();
        }
        byte tmpRead = in.readByte();
        read = tmpRead == 0 ? null : tmpRead == 1;
        byte tmpImportant = in.readByte();
        important = tmpImportant == 0 ? null : tmpImportant == 1;
        childName = in.readString();
        notifCode = in.readString();
    }

    public static final Creator<NotificationInformation> CREATOR = new Creator<NotificationInformation>() {
        @Override
        public NotificationInformation createFromParcel(Parcel in) {
            return new NotificationInformation(in);
        }

        @Override
        public NotificationInformation[] newArray(int size) {
            return new NotificationInformation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(message);
        if (dateMillis == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(dateMillis);
        }
        dest.writeByte((byte) (read == null ? 0 : read ? 1 : 2));
        dest.writeByte((byte) (important == null ? 0 : important ? 1 : 2));
        dest.writeString(childName);
        dest.writeString(notifCode);
    }
}
