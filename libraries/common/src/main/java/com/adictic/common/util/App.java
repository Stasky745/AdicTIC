package com.adictic.common.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.adictic.common.BuildConfig;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.data.StringFormat;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {

    private static Resources res;

    private static Drawable adminPic = null;
    public static Drawable getAdminPic() { return adminPic; }
    public static void setAdminPic(Drawable d) { adminPic = d; }

    private static SharedPreferences sharedPreferences = null;

    public static SharedPreferences getSharedPreferences() { return  sharedPreferences; }
    public static void setSharedPreferences(SharedPreferences sharedPreferences1) { sharedPreferences = sharedPreferences1; }

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
    }

    public static Resources getRes(){
        return res;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class)
                .withDeleteReportsOnAppUpdate(true)
                .withStacktraceLimit(1)
                .withResetLimitsOnAppUpdate(true);
        ACRA.init(this, builder);
    }
}
