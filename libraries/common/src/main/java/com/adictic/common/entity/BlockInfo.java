package com.adictic.common.entity;

import java.util.ArrayList;
import java.util.Collection;

public class BlockInfo {
    public Collection<TimeBlock> tempsBloqueig;
    public Collection<TimeFreeUse> tempsFreeUse;
    public Collection<IntentsAccesApp> intentsAccesApps;
    public Collection<Long> intentsAccesDisps;

    public BlockInfo(){
        tempsBloqueig = new ArrayList<>();
        tempsFreeUse = new ArrayList<>();
        intentsAccesApps = new ArrayList<>();
        intentsAccesDisps = new ArrayList<>();
    }
}
