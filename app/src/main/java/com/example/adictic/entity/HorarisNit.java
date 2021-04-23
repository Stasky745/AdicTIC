package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class HorarisNit {
    public Integer idDia;

    // Joda-time getMillisOfDay()
    public Integer dormir;
    public Integer despertar;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Integer)
            return obj.equals(this.idDia);
        if(obj instanceof HorarisNit)
            return this.idDia.equals(((HorarisNit) obj).idDia);
        else return false;
    }
}
