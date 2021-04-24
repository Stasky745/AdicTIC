package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class BlockedApp {
    public String pkgName;

    // -1 si bloqueig permanent
    public long timeLimit;

    public boolean blockedNow;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return obj.equals(this.pkgName);
        if(obj instanceof BlockedApp) return ((BlockedApp) obj).pkgName.equals(this.pkgName);
        else return false;
    }
}
