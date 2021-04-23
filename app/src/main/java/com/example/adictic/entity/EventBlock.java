package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class EventBlock {
    public long id;

    public String name;

    // Joda-time getMillisOfDay()
    public int startEvent;
    public int endEvent;

    public boolean activeNow;

    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;
    public boolean sunday;

    public EventBlock(EventBlock other){
        this.id = other.id;
        this.name = other.name;

        this.startEvent = other.startEvent;
        this.endEvent = other.endEvent;

        this.activeNow = other.activeNow;

        this.monday = other.monday;
        this.tuesday = other.tuesday;
        this.wednesday = other.wednesday;
        this.thursday = other.thursday;
        this.friday = other.friday;
        this.saturday = other.saturday;
        this.sunday = other.sunday;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
//        return super.equals(obj);


        if(obj instanceof EventBlock){
            EventBlock object = (EventBlock) obj;
            return this.id == object.id;
        }
        else return false;
    }

    public boolean exactSame(EventBlock object){
        return this.id == object.id &&
                this.monday == object.monday &&
                this.tuesday == object.tuesday &&
                this.wednesday == object.wednesday &&
                this.thursday == object.thursday &&
                this.friday == object.friday &&
                this.saturday == object.saturday &&
                this.sunday == object.sunday &&
                this.activeNow == object.activeNow &&
                this.name.equals(object.name) &&
                this.startEvent == object.startEvent &&
                this.endEvent == object.endEvent;
    }

    public EventBlock(){
        id = 0;
        name = "";
        startEvent = 0;
        endEvent = 0;

        monday = false;
        tuesday = false;
        wednesday = false;
        thursday = false;
        friday = false;
        saturday = false;
        sunday = false;
    }
}
