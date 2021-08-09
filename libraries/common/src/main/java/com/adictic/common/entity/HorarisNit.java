package com.adictic.common.entity;

import androidx.annotation.Nullable;

public class HorarisNit {
    public Integer dia;

    // Joda-time getMillisOfDay()
    public Integer dormir;
    public Integer despertar;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Integer)
            return obj.equals(this.dia);
        if(obj instanceof HorarisNit)
            return this.dia.equals(((HorarisNit) obj).dia);
        else
            return false;
    }
}
