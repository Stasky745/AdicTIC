package com.adictic.client.util.hilt;

import android.content.Context;

import com.adictic.client.rest.AdicticApi;
import com.adictic.common.BuildConfig;
import com.adictic.common.util.AdicticAuthenticator;
import com.adictic.common.util.Global;
import com.adictic.common.util.hilt.NetworkModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AdicticNetworkModule extends NetworkModule {

    @Provides
    @Singleton
    public static AdicticApi provideAdicticApi(@ApplicationContext Context context) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Retrofit retrofit = new Retrofit.Builder()
                .client(getOkHttpClient(new AdicticAuthenticator(context)))
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(AdicticApi.class);
    }
}
