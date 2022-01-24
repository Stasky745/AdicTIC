package com.adictic.common.util.hilt;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class GlideModule {

    @Provides
    @Singleton
    public static RequestManager provideGlideRequestManager(@ApplicationContext Context context) {
        return Glide.with(context);
    }
}
