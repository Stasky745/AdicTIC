package com.example.adictic.entity;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public long id;
    public Integer tutor;
    public List<FillNom> llista;
}