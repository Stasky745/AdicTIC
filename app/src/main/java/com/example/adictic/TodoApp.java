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

    private static long ID=-1;
    private static Integer tutor=0;
    private static String tutorToken = null;
    private static int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    private static boolean freeUse = false;
    private static long startFreeUse = 0;
    private static boolean blockedDevice = false;
    private static boolean liveApp = false;
    private static String currentAppKid = null;
    private static String timeOpenedCurrentAppKid = null;

    private static Map<String,Long> limitApps = new HashMap<>();
    private static List<String> blockedApps = new ArrayList<>();

    public static void setStartFreeUse(long l){startFreeUse = l;}
    public static long getStartFreeUse(){return startFreeUse;}

    public static void setTutorToken(String s){ TodoApp.tutorToken = s; }
    public static String getTutorToken(){ return TodoApp.tutorToken; }

    public static void setID(Long l){ TodoApp.ID=l; }
    public static Long getID(){ return TodoApp.ID; }

    public static void setTutor(Integer i){ TodoApp.tutor=i; }
    public static Integer getTutor(){ return TodoApp.tutor; }

    public static void setDayOfYear(int i){ TodoApp.dayOfYear=i; }
    public static int getDayOfYear(){ return TodoApp.dayOfYear; }

    public static void setFreeUse(boolean b){ TodoApp.freeUse=b; }
    public static boolean getFreeUse(){ return TodoApp.freeUse; }

    public static void setLiveApp(boolean b){ TodoApp.liveApp=b; }
    public static boolean getLiveApp(){ return TodoApp.liveApp; }

    public static void setCurrentAppKid(String s){ TodoApp.currentAppKid = s; }
    public static String getCurrentAppKid(){ return TodoApp.currentAppKid; }

    public static void setTimeOpenedCurrentAppKid(String s){ TodoApp.timeOpenedCurrentAppKid = s; }
    public static String getTimeOpenedCurrentAppKid(){ return TodoApp.timeOpenedCurrentAppKid; }

    public static void setLimitApps(Map<String,Long> m){ TodoApp.limitApps = m; }
    public static Map<String,Long> getLimitApps(){ return TodoApp.limitApps; }

    public static void setBlockedDevice(boolean b){ TodoApp.blockedDevice=b; }
    public static boolean getBlockedDevice(){ return TodoApp.blockedDevice; }

    public static void setBlockedApps(List<String> l){ TodoApp.blockedApps=l; }
    public static List<String> getBlockedApps(){ return TodoApp.blockedApps; }

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
