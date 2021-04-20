package com.example.adictic.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HorarisNit {
    @PrimaryKey
    public int idDia;

    // Joda-time getMillisOfDay()
    public int dormir;
    public int despertar;
}
