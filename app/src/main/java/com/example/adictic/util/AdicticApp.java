package com.example.adictic.util;

import android.content.SharedPreferences;

import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Global;
import com.example.adictic.BuildConfig;
import com.example.adictic.rest.AdicticApi;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdicticApp extends App {

    private AdicticApi mTodoService;

    public static SharedPreferences sharedPreferences = null;
    public static SharedPreferences getSharedPreferences() { return  sharedPreferences; }
    public static void setSharedPreferences(SharedPreferences sharedPreferences1) { sharedPreferences = sharedPreferences1; }

    public static String[] newFeatures = {

    };

    public static String[] fixes = {

    };

    public static String[] changes = {
            "Canvis en l'activitat de moments lliures de tecnologies"
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

        mTodoService = retrofit.create(AdicticApi.class);
    }

    public AdicticApi getAPI() {
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

                    if (sharedPreferences == null)
                        return null;

                    String username = sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME,null);
                    String password = sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD, null);

                    if(username != null && password != null) {
                        System.out.println("Authenticating for response: " + response);
                        System.out.println("Challenges: " + response.challenges());

                        UserLogin userLogin = new UserLogin();
                        userLogin.username = username;
                        userLogin.password = password;
                        userLogin.tutor = sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false) ? 1 : 0;
                        userLogin.token = sharedPreferences.getString(Constants.SHARED_PREFS_TOKEN, "");

                        String gson = new Gson().toJson(userLogin);
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        RequestBody body = RequestBody.create(gson, JSON);

                        String url = BuildConfig.DEBUG ? Global.BASE_URL_DEBUG : Global.BASE_URL_RELEASE;
                        url += "users/login";

                        return response.request().newBuilder()
                                .url(url)
                                .post(body)
                                .build();
                    }

                    return null;
                })
                .build();
    }
}
