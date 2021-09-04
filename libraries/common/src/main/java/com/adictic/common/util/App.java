package com.adictic.common.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.adictic.common.BuildConfig;
import com.adictic.common.rest.Api;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.data.StringFormat;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    private Api api;

    private static Drawable adminPic = null;
    public static Drawable getAdminPic() { return adminPic; }
    public static void setAdminPic(Drawable d) { adminPic = d; }

    private static SharedPreferences sharedPreferences = null;

    public static SharedPreferences getSharedPreferences() { return  sharedPreferences; }
    public static void setSharedPreferences(SharedPreferences sharedPreferences1) { sharedPreferences = sharedPreferences1; }

    protected Retrofit createRetrofit(OkHttpClient httpClient){
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(Api.class);

        return retrofit;
    }

    public Api getAPI(){ return api; }

    protected OkHttpClient getOkHttpClient(Authenticator authenticator) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClient.addInterceptor(interceptor);
        }

        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

        return httpClient
                .cookieJar(cookieJar)
                .authenticator(authenticator)
                .build();
    }

    protected int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
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
