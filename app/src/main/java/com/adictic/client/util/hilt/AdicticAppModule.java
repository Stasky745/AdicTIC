package com.adictic.client.util.hilt;

import android.app.Application;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import androidx.work.WorkManager;

import com.adictic.client.service.ClientNotificationManager;
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
    public static WorkManager provideWorkManager(@ApplicationContext Context application) {
        return WorkManager.getInstance(application);
    }

    @Provides
    @Singleton
    public static UsageStatsManager provideUsageStatsManager(@ApplicationContext Context application) {
        return (UsageStatsManager) application.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Provides
    @Singleton
    public static ClientNotificationManager provideNotificationManager(@ApplicationContext Context application) {
        return new ClientNotificationManager(application);
    }
}
