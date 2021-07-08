package com.adictic.common.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import com.adictic.common.entity.FillNom;

import java.util.ArrayList;

public class MainActivityAbstractClass extends AppCompatActivity implements MainActivityInterface {
    private ArrayList<FillNom> homeParent_childs = null;
    private Long homeParent_lastChildsUpdate = null;

    @Override
    public void setHomeParent_childs(ArrayList<FillNom> list) {
        homeParent_childs = list;
    }

    @Override
    public ArrayList<FillNom> getHomeParent_childs() {
        return homeParent_childs;
    }

    @Override
    public void setHomeParent_lastChildsUpdate(Long childId) {
        homeParent_lastChildsUpdate = childId;
    }

    @Override
    public Long getHomeParent_lastChildsUpdate() {
        return homeParent_lastChildsUpdate;
    }
}
