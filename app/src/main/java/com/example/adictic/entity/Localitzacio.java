package com.example.adictic.entity;

public class Localitzacio {

    public Localitzacio(Long id, String poblacio){
        this.id = id;
        this.poblacio = poblacio;
    }

    public Long id;
    public String poblacio;

    @Override
    public String toString() {
        return poblacio;
    }
}
