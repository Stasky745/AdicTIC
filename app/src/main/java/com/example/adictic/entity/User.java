package com.example.adictic.entity;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public long id;
    public int tutor;
    public int existeix;
    public List<FillNom> llista;
}
