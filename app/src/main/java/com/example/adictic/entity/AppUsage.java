package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class AppUsage {
    public AppInfo app;
    public Long lastTimeUsed;
    public Long totalTime;

    @Override
    public boolean equals(@Nullable Object obj) {
        AppUsage au = (AppUsage) obj;
        if(obj instanceof String) return app.pkgName.equals(obj);
        if (au == null || getClass() != obj.getClass()) return false;
        return app.pkgName.equals(au.app.pkgName);
    }
}
