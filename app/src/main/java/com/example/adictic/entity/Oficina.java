package com.example.adictic.entity;

import android.location.Address;

import androidx.annotation.NonNull;

public class Oficina {
    public Long id;
    public String name;
    public Double latitude;
    public Double longitude;
    public String address;
    public String ciutat;
    public String desc;
    public String telf;

    @NonNull
    @Override
    public String toString() {
        return name + " (" + ciutat.toUpperCase() + ")";
    }
}
