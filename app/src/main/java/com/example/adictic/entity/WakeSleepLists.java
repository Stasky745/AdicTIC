package com.example.adictic.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class WakeSleepLists implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WakeSleepLists> CREATOR = new Parcelable.Creator<WakeSleepLists>() {
        @Override
        public WakeSleepLists createFromParcel(Parcel in) {
            return new WakeSleepLists(in);
        }

        @Override
        public WakeSleepLists[] newArray(int size) {
            return new WakeSleepLists[size];
        }
    };
    public TimeDay wake;
    public TimeDay sleep;
    public int tipus; // 1-diari ; 2-setmana; 3-generic

    public WakeSleepLists() {
        wake = new TimeDay();
        sleep = new TimeDay();
        tipus = 1;
    }

    protected WakeSleepLists(Parcel in) {
        wake = (TimeDay) in.readValue(TimeDay.class.getClassLoader());
        sleep = (TimeDay) in.readValue(TimeDay.class.getClassLoader());
        tipus = in.readInt();
    }

    public boolean isEmpty() {
        return wake.isEmpty() && sleep.isEmpty() && tipus == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(wake);
        dest.writeValue(sleep);
        dest.writeInt(tipus);
    }
}