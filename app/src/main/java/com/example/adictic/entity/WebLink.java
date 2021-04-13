package com.example.adictic.entity;

import androidx.annotation.Nullable;

public class WebLink {
    public String name;
    public String url;

    @Override
    public boolean equals(@Nullable Object obj) {
        WebLink webLink = (WebLink) obj;
        assert webLink != null;
        return this.name.equals(webLink.name) && this.url.equals(webLink.url);
    }
}
