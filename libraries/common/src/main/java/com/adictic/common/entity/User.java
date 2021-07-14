package com.adictic.common.entity;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public long id;
    public Long adminId;
    public Integer tutor;
    public List<FillNom> llista;
    public String name; //S'ha de treure despres de canviar lo de ChatInfo
}
