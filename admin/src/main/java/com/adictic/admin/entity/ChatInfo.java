package com.adictic.admin.entity;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ChatInfo implements Parcelable {
    public Long userId;
    public Long childId;
    public String username;
    public Boolean hasAccess;
    public String lastMessage;
    public Date timeLastMessage;

    protected ChatInfo(Parcel in) {
        long tmpId = in.readLong();
        userId = tmpId == -1 ? null : tmpId;

        long tmpId2 = in.readLong();
        childId = tmpId2 == -1 ? null : tmpId2;

        username = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasAccess = in.readBoolean();
        }
        else {
            byte tmpHasAccess = in.readByte();
            hasAccess = tmpHasAccess == 0 ? null : tmpHasAccess == 1;
        }
        lastMessage = in.readString();
        long tmpLastMessage = in.readLong();
        timeLastMessage = tmpLastMessage == -1 ? null : new Date(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId != null ? userId : -1);
        dest.writeLong(childId != null ? childId : -1);
        dest.writeString(username);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(hasAccess);
        }
        else
            dest.writeByte((byte) (hasAccess == null ? 0 : hasAccess ? 1 : 2));
        dest.writeString(lastMessage);
        dest.writeLong(timeLastMessage != null ? timeLastMessage.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
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
}
