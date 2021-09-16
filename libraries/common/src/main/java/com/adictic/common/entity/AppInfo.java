package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Comparable, Parcelable {
    public String pkgName;
    public String appName;
    public Integer category;

    public AppInfo(){ }

    protected AppInfo(Parcel in) {
        pkgName = in.readString();
        appName = in.readString();
        if (in.readByte() == 0) {
            category = null;
        } else {
            category = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        dest.writeString(appName);
        if (category == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(category);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public int compareTo(Object o) {
        return this.pkgName.compareTo(((AppInfo) o).pkgName);
    }
}
