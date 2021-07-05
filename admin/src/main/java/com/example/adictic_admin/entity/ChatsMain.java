package com.example.adictic_admin.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ChatsMain implements Parcelable {
    public List<ChatInfo> oberts;
    public List<ChatInfo> tancats;

    protected ChatsMain(Parcel in) {
        oberts = in.createTypedArrayList(ChatInfo.CREATOR);
        tancats = in.createTypedArrayList(ChatInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(oberts);
        dest.writeTypedList(tancats);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatsMain> CREATOR = new Creator<ChatsMain>() {
        @Override
        public ChatsMain createFromParcel(Parcel in) {
            return new ChatsMain(in);
        }

        @Override
        public ChatsMain[] newArray(int size) {
            return new ChatsMain[size];
        }
    };
}
