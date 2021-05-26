package com.example.adictic.util;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.adictic.BuildConfig;
import com.example.adictic.rest.TodoApi;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TodoApp extends Application {

    private TodoApi mTodoService;
    private static SharedPreferences sharedPreferences = null;

    public static SharedPreferences getSharedPreferences() { return  sharedPreferences; }
    public static void setSharedPreferences(SharedPreferences sharedPreferences1) { sharedPreferences = sharedPreferences1; }

    public static String[] newFeatures = {
            "Implementació d'aquest llistat de novetats a l'actualització"
    };

    public static String[] fixes = {

    };

    public static String[] changes = {
            "Nou calendari a \"Veure ús d'aplicacions\""
    };

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient httpClient = getOkHttpClient();

        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(URL)
                //.baseUrl(Global.BASE_URL_GENYMOTION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mTodoService = retrofit.create(TodoApi.class);
    }

    public TodoApi getAPI() {
        return mTodoService;
    }

    public OkHttpClient getOkHttpClient() {

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
                .authenticator((route, response) -> {
                    if (responseCount(response) >= 3) {
                        return null; // If we've failed 3 times, give up.
                    }

                    String username = sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME,null);
                    String password = sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD, null);

                    if(username != null && password != null) {
                        System.out.println("Authenticating for response: " + response);
                        System.out.println("Challenges: " + response.challenges());
                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }

                    return null;
                })
                .build();
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
