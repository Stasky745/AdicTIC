package com.example.adictic;

import android.app.Application;

import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Global;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TodoApp extends Application {

    TodoApi mTodoService;

    private long ID=-1;
    private int tutor=0;
    private String tutorToken = null;
    private int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    private boolean freeUse = false;
    private boolean blockedDevice = false;
    private boolean liveApp = false;
    private String currentAppKid = null;
    private String timeOpenedCurrentAppKid = null;

    private Map<String,Long> limitApps = new HashMap<>();
    private List<String> blockedApps = new ArrayList<>();

    public void setTutorToken(String s){ tutorToken = s; }
    public String getTutorToken(){ return tutorToken; }

    public void setID(Long l){ ID=l; }
    public Long getID(){ return ID; }

    public void setTutor(int i){ tutor=i; }
    public int getTutor(){ return tutor; }

    public void setDayOfYear(int i){ dayOfYear=i; }
    public int getDayOfYear(){ return dayOfYear; }

    public void setFreeUse(boolean b){ freeUse=b; }
    public boolean getFreeUse(){ return freeUse; }

    public void setLiveApp(boolean b){ liveApp=b; }
    public boolean getLiveApp(){ return liveApp; }

    public void setCurrentAppKid(String s){ currentAppKid = s; }
    public String getCurrentAppKid(){ return currentAppKid; }

    public void setTimeOpenedCurrentAppKid(String s){ timeOpenedCurrentAppKid = s; }
    public String getTimeOpenedCurrentAppKid(){ return timeOpenedCurrentAppKid; }

    public void setLimitApps(Map<String,Long> m){ limitApps = m; }
    public Map<String,Long> getLimitApps(){ return limitApps; }

    public void setBlockedDevice(Boolean b){ blockedDevice=b; }
    public boolean getBlockedDevice(){ return blockedDevice; }

    public void setBlockedApps(List<String> l){ blockedApps=l; }
    public List<String> getBlockedApps(){ return blockedApps; }

    @Override
    public void onCreate() {
        super.onCreate();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(Global.BASE_URL_PORTFORWARDING)
                //.baseUrl(Global.BASE_URL_GENYMOTION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mTodoService = retrofit.create(TodoApi.class);
    }

    public TodoApi getAPI() {
        return mTodoService;
    }
}
