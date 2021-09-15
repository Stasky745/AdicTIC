package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class AppUsage implements Parcelable {
    public AppInfo app;
    public Long lastTimeUsed;
    public Long totalTime;

    public AppUsage(){ }

    protected AppUsage(Parcel in) {
        if (in.readByte() == 0) {
            lastTimeUsed = null;
        } else {
            lastTimeUsed = in.readLong();
        }
        if (in.readByte() == 0) {
            totalTime = null;
        } else {
            totalTime = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (lastTimeUsed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(lastTimeUsed);
        }
        if (totalTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(totalTime);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppUsage> CREATOR = new Creator<AppUsage>() {
        @Override
        public AppUsage createFromParcel(Parcel in) {
            return new AppUsage(in);
        }

        @Override
        public AppUsage[] newArray(int size) {
            return new AppUsage[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return app.pkgName.equals(obj);
        if (obj == null || getClass() != obj.getClass()) return false;
        AppUsage au = (AppUsage) obj;
        return app.pkgName.equals(au.app.pkgName);
    }
}
