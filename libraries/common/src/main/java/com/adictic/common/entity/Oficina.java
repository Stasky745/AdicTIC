package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Oficina implements Parcelable {
    public Long id;
    public String name;
    public Double latitude;
    public Double longitude;
    public String address;
    public String ciutat;
    public String description;
    public String telf;
    public String website;
    public Long officeAdminId;
    public String officeAdminName;

    protected Oficina(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        address = in.readString();
        ciutat = in.readString();
        description = in.readString();
        telf = in.readString();
        website = in.readString();
        if (in.readByte() == 0) {
            officeAdminId = null;
        } else {
            officeAdminId = in.readLong();
        }
        if (in.readByte() == 0) {
            officeAdminName = null;
        } else {
            officeAdminName = in.readString();
        }
    }

    public static final Creator<Oficina> CREATOR = new Creator<Oficina>() {
        @Override
        public Oficina createFromParcel(Parcel in) {
            return new Oficina(in);
        }

        @Override
        public Oficina[] newArray(int size) {
            return new Oficina[size];
        }
    };

    @Override
    public String toString() {
        return "Oficina{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", ciutat='" + ciutat + '\'' +
                ", description='" + description + '\'' +
                ", telf='" + telf + '\'' +
                ", website='" + website + '\'' +
                ", officeAdminId=" + officeAdminId +
                ", officeAdminName='" + officeAdminName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(name);
        if (latitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(latitude);
        }
        if (longitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(longitude);
        }
        parcel.writeString(address);
        parcel.writeString(ciutat);
        parcel.writeString(description);
        parcel.writeString(telf);
        parcel.writeString(website);
        if (officeAdminId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(officeAdminId);
        }
        if (officeAdminName == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(officeAdminName);
        }
    }

    public Oficina(Long id, Oficina oficinaNova){
        super();
        this.id = id;
        this.name = oficinaNova.name;
        this.address = oficinaNova.address;
        this.ciutat = oficinaNova.ciutat;
        this.description = oficinaNova.description;
        this.latitude = oficinaNova.latitude;
        this.longitude = oficinaNova.longitude;
        this.telf = oficinaNova.telf;
        this.website = oficinaNova.website;
        this.officeAdminId = oficinaNova.officeAdminId;
        this.officeAdminName = oficinaNova.officeAdminName;
    }

    public Oficina() {
        id = null;
        name = "";
        latitude = 0.0;
        longitude = 0.0;
        address = "";
        ciutat = "";
        description = "";
        telf = "";
        website = "";
        officeAdminId = -1L;
        officeAdminName = "";
    }

    public Boolean hiHaCanvis(Object o){
        if(o instanceof Oficina){
            Oficina oficina = (Oficina) o;

            if (name != null && !name.equals(oficina.name))
                return true;
            if (latitude != null && !latitude.equals(oficina.latitude))
                return true;
            if (longitude != null && !longitude.equals(oficina.longitude))
                return true;
            if (address != null && !address.equals(oficina.address))
                return true;
            if (ciutat != null && !ciutat.equals(oficina.ciutat))
                return true;
            if (description != null && !description.equals(oficina.description))
                return true;
            if (telf != null && !telf.equals(oficina.telf))
                return true;
            return website != null && !website.equals(oficina.website);
        }
        else return null;
    }
}
