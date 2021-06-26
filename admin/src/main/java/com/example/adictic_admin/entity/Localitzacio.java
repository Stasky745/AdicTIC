package com.example.adictic_admin.entity;

import androidx.annotation.Nullable;

public class Localitzacio {
    public Long id;
    public String poblacio;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null)
            return false;

        if(obj instanceof String)
            return obj.equals(poblacio);

        if (obj instanceof Localitzacio)
            return this.id.equals(((Localitzacio) obj).id);

        return false;
    }
}
