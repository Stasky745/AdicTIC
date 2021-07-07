package com.adictic.common.entity;

public class AppInfo implements Comparable {
    public String pkgName;
    public String appName;
    public Integer category;


    @Override
    public int compareTo(Object o) {
        return this.pkgName.compareTo(((AppInfo) o).pkgName);
    }
}
