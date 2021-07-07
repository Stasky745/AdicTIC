package com.adictic.common.entity;

import androidx.annotation.NonNull;

public class GeoFill {
    public String nom;
    public Long id;
    public Double latitud;
    public Double longitud;
    public String hora;

    @NonNull
    @Override
    public String toString() {
        return nom;
    }
}
