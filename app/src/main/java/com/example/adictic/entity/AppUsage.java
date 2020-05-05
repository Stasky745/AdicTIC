package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class AppUsage {
    public String pkgName;
    public String appTitle;
    public Long lastTimeUsed;
    public Long totalTime;
    public Integer category;

    @Override
    public boolean equals(@Nullable Object obj) {
        AppUsage au = (AppUsage) obj;
        if(au == null || getClass() != obj.getClass()) return false;
        return pkgName.equals(au.pkgName);
    }
}
