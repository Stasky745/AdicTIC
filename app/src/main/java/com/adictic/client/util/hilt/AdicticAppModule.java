package com.adictic.client.util.hilt;

import android.app.Application;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import androidx.work.WorkManager;

import com.adictic.common.util.App;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AdicticAppModule {

    @Provides
    @Singleton
    public static WorkManager provideWorkManager(@ApplicationContext Application application) {
        return WorkManager.getInstance(application);
    }

    @Provides
    @Singleton
    public static UsageStatsManager provideUsageStatsManager(@ApplicationContext Application application) {
        return (UsageStatsManager) application.getSystemService(Context.USAGE_STATS_SERVICE);
    }
}
