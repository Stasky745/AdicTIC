package com.example.adictic.roomdb;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HorarisNit {
    @PrimaryKey
    public Integer idDia;

    // Joda-time getMillisOfDay()
    public Integer dormir;
    public Integer despertar;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof HorarisNit)
            return this.idDia.equals(((HorarisNit) obj).idDia);
        else return false;
    }
}
