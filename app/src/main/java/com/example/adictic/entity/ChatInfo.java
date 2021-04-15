package com.example.adictic.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatInfo implements Parcelable {
    public AdminProfile admin;
    public String lastMessage;

    protected ChatInfo(Parcel in) {
        admin = in.readParcelable(AdminProfile.class.getClassLoader());
        lastMessage = in.readString();
    }

    public static final Creator<ChatInfo> CREATOR = new Creator<ChatInfo>() {
        @Override
        public ChatInfo createFromParcel(Parcel in) {
            return new ChatInfo(in);
        }

        @Override
        public ChatInfo[] newArray(int size) {
            return new ChatInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(admin, i);
        parcel.writeString(lastMessage);
    }
}