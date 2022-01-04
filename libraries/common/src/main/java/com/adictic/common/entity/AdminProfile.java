package com.adictic.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AdminProfile implements Parcelable {
    public Long idUser;
    public Long idAdmin;
    public String name;
    public String professio;
    public String description;
    public List<WebLink> webLinks;
    public Oficina oficina;

    public AdminProfile(){ }

    protected AdminProfile(Parcel in) {
        if (in.readByte() == 0) {
            idUser = null;
        } else {
            idUser = in.readLong();
        }
        if (in.readByte() == 0) {
            idAdmin = null;
        } else {
            idAdmin = in.readLong();
        }
        name = in.readString();
        professio = in.readString();
        description = in.readString();

        //Llegir Weblinks
        webLinks = new ArrayList<>();
        int linkSize = in.readInt();
        for (int i = 0; i < linkSize; i++){
            webLinks.add(in.readParcelable(WebLink.class.getClassLoader()));
        }

        oficina = in.readParcelable(Oficina.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (idUser == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(idUser);
        }
        if (idAdmin == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(idAdmin);
        }
        dest.writeString(name);
        dest.writeString(professio);
        dest.writeString(description);

        //Escriure WebLinks
        dest.writeInt(webLinks.size());
        for (int j = 0; j < webLinks.size(); j++){
            dest.writeParcelable(webLinks.get(j),flags);
        }

        dest.writeParcelable(oficina, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AdminProfile> CREATOR = new Creator<AdminProfile>() {
        @Override
        public AdminProfile createFromParcel(Parcel in) {
            return new AdminProfile(in);
        }

        @Override
        public AdminProfile[] newArray(int size) {
            return new AdminProfile[size];
        }
    };

    @Override
    public String toString() {
        return "AdminProfile{" +
                "idUser=" + idUser +
                ", idAdmin=" + idAdmin +
                ", name='" + name + '\'' +
                ", professio='" + professio + '\'' +
                ", description='" + description + '\'' +
                ", webLinks=" + webLinks.toString() +
                ", oficina=" + oficina +
                '}';
    }
}
