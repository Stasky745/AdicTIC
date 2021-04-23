package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class FreeUseApp {
    public String pkgName = "";

    public long millisUsageStart;
    public long millisUsageEnd;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return obj.equals(pkgName);
        else if(obj instanceof FreeUseApp) return ((FreeUseApp) obj).pkgName.equals(this.pkgName);
        else return false;
    }
}
