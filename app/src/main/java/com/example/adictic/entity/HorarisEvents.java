package com.example.adictic.entity;

import androidx.annotation.Nullable;

import java.util.Comparator;
import java.util.List;

public class HorarisEvents {
    public Long id;
    public String name;
    public String start;
    public String finish;
    public List<Integer> days;

    @Override
    public boolean equals(@Nullable Object obj) {
        assert obj != null;
        if(getClass() != obj.getClass()) return false;

        HorarisEvents he = (HorarisEvents) obj;
        return id.equals(he.id);
    }

    public boolean exactSame(HorarisEvents he) {
        return id.equals(he.id) && name.equals(he.name) && start.equals(he.start) && finish.equals(he.finish) && days.equals(he.days);
    }
}
