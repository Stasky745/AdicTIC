package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class AppUsage {
    public String pkgName;
    public String appTitle;
    public Long lastTimeUsed;
    public Long totalTime;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if(obj == null || (getClass() != obj.getClass() && obj.getClass()!=String.class)) return false;
        if(obj.getClass()==String.class) return pkgName.equals((String) obj);

        AppUsage that = (AppUsage) obj;
        return pkgName.equals(that.pkgName);
    }
}
