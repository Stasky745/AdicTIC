package com.adictic.common.entity;

public class Localitzacio {

    public Long id;
    public String poblacio;
    public Localitzacio(Long id, String poblacio) {
        this.id = id;
        this.poblacio = poblacio;
    }

    @Override
    public String toString() {
        return poblacio;
    }
}
