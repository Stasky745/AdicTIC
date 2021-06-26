package com.example.adictic_admin.entity;

import androidx.annotation.Nullable;

public class AppUsage {
    public AppInfo app;
    public Long lastTimeUsed;
    public Long totalTime;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return app.pkgName.equals(obj);
        if (obj == null || getClass() != obj.getClass()) return false;
        AppUsage au = (AppUsage) obj;
        return app.pkgName.equals(au.app.pkgName);
    }
}
