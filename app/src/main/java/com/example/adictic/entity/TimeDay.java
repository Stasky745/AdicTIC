package com.example.adictic.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeDay implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TimeDay> CREATOR = new Parcelable.Creator<TimeDay>() {
        @Override
        public TimeDay createFromParcel(Parcel in) {
            return new TimeDay(in);
        }

        @Override
        public TimeDay[] newArray(int size) {
            return new TimeDay[size];
        }
    };
    public String monday;
    public String tuesday;
    public String wednesday;
    public String thursday;
    public String friday;
    public String saturday;
    public String sunday;

    public TimeDay() {
        monday = "";
        tuesday = "";
        wednesday = "";
        thursday = "";
        friday = "";
        saturday = "";
        sunday = "";
    }

    protected TimeDay(Parcel in) {
        monday = in.readString();
        tuesday = in.readString();
        wednesday = in.readString();
        thursday = in.readString();
        friday = in.readString();
        saturday = in.readString();
        sunday = in.readString();
    }

    public boolean isEmpty() {
        if (!monday.equals("")) return false;
        else if (!tuesday.equals("")) return false;
        else if (!wednesday.equals("")) return false;
        else if (!thursday.equals("")) return false;
        else if (!friday.equals("")) return false;
        else if (!saturday.equals("")) return false;
        else return sunday.equals("");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(monday);
        dest.writeString(tuesday);
        dest.writeString(wednesday);
        dest.writeString(thursday);
        dest.writeString(friday);
        dest.writeString(saturday);
        dest.writeString(sunday);
    }
}
