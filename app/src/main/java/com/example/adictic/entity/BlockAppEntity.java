package com.example.adictic.entity;

import okhttp3.MultipartBody;

public class BlockAppEntity implements Comparable<BlockAppEntity> {
    public String pkgName;
    public String appName;
    public Integer appCategory;
    public Long appTime;
    public MultipartBody appIcon;

    @Override
    public int compareTo(BlockAppEntity e) {
        if(this.appTime >= 0 && e.appTime < 0) return -1;
        else if(this.appTime < 0 && e.appTime >= 0) return 1;
        else return this.appName.compareTo(e.appName);
    }
}
