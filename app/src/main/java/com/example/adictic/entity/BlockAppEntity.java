package com.example.adictic.entity;

public class BlockAppEntity implements Comparable<BlockAppEntity> {
    public String pkgName;
    public String appName;
    public Integer appCategory;
    public Long appTime;

    @Override
    public int compareTo(BlockAppEntity e) {
        // Si les dues estan bloquejades
        if(this.appTime == 0 && e.appTime == 0)
            return this.appName.compareTo(e.appName);

        // Si la primera està bloquejada
        if(this.appTime == 0)
            return -1;

        // Si la segona està bloquejada
        if(e.appTime == 0)
            return 1;

        // Si la primera està limitada i la segona no
        if (this.appTime > 0 && e.appTime < 0)
            return -1;

        // Si la segona està limitada i la primera no
        if (this.appTime < 0 && e.appTime > 0)
            return 1;

        // Si les dues estan limitades
        if(this.appTime > 0 && e.appTime > 0 && !this.appTime.equals(e.appTime))
            return Long.compare(this.appTime,e.appTime);

        return this.appName.compareTo(e.appName);
    }
}
