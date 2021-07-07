package com.adictic.common.ui.main;

import com.adictic.common.entity.FillNom;
import com.adictic.common.entity.LiveApp;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public interface MainActivityInterface {

    Integer tempsPerActu = 5 * 60 * 1000; // 5 min
    Integer tempsPerActuLiveApp = 60 * 1000; // 1 minut

    TreeMap<Long, Long> mainParent_lastAppUsedUpdate = new TreeMap<>();
    TreeMap<Long, LiveApp> mainParent_lastAppUsed = new TreeMap<>();
    TreeMap<Long, Long> mainParent_lastUsageChartUpdate = new TreeMap<>();
    TreeMap<Long, Map<String, Long>> mainParent_usageChart = new TreeMap<>();
    TreeMap<Long, Long> mainParent_totalUsageTime = new TreeMap<>();

    ArrayList<FillNom> homeParent_childs = null;
    void setHomeParent_childs(ArrayList<FillNom> list);
    ArrayList<FillNom> getHomeParent_childs();



    Long homeParent_lastChildsUpdate = null;
    void setHomeParent_lastChildsUpdate(Long childId);
    Long getHomeParent_lastChildsUpdate();
}
