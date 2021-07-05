package com.example.adictic_admin.entity;

public class AppInfo implements Comparable<Object> {
    public String pkgName;
    public String appName;
    public Integer category;


    @Override
    public int compareTo(Object o) {
        return this.pkgName.compareTo(((AppInfo) o).pkgName);
    }
}
