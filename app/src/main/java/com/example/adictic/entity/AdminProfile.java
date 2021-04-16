package com.example.adictic.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AdminProfile implements Parcelable {
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
    public Long idUser;
    public Long idAdmin;
    public String name;
    public String professio;
    public String description;
    public List<WebLink> webLinks;
    public Long idOficina;

    protected AdminProfile(Parcel in) {
        idUser = in.readLong();
        idAdmin = in.readLong();
        name = in.readString();
        professio = in.readString();
        description = in.readString();

        //Llegir Weblinks
        webLinks = new ArrayList<>();
        int linkSize = in.readInt();
        for (int i = 0; i < linkSize; i++){
            webLinks.add(in.readParcelable(WebLink.class.getClassLoader()));
        }

        idOficina = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(idUser);
        parcel.writeLong(idAdmin);
        parcel.writeString(name);
        parcel.writeString(professio);
        parcel.writeString(description);

        //Escriure WebLinks
        parcel.writeInt(webLinks.size());
        for (int j = 0; j < webLinks.size(); j++){
            parcel.writeParcelable(webLinks.get(j),i);
        }

        parcel.writeLong(idOficina);
    }
}
