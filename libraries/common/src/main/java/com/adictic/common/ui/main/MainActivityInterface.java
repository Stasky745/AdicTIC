package com.adictic.common.ui.main;

import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.FillNom;
import com.adictic.common.entity.LiveApp;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public interface MainActivityInterface {

    Integer tempsPerActu = 5 * 60 * 1000; // 5 min

    void setHomeParent_childs(ArrayList<FillNom> list);
    ArrayList<FillNom> getHomeParent_childs();


    void setHomeParent_lastChildsUpdate(Long childId);
    Long getHomeParent_lastChildsUpdate();
}
