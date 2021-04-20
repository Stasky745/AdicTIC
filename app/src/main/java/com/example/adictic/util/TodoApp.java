package com.example.adictic.util;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import com.example.adictic.R;
import com.example.adictic.entity.GeoFill;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.entity.Oficina;
import com.example.adictic.rest.TodoApi;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TodoApp extends Application {

    public final static List<String> blackListLiveApp = Collections.singletonList("com.google.android.apps.nexuslauncher");
    private static final List<String> blockEvents = new ArrayList<>();
    private static boolean geolocOpen = false;
    private static long IDTutor = -1;
    private static Integer tutor = -1;
    private static long IDChild = -1;
    private static int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    private static boolean freeUse = false;
    private static long startFreeUse = 0;
    private static boolean blockedDevice = false;
    private static boolean liveApp = false;
    private static String currentAppKid = null;
    private static String timeOpenedCurrentAppKid = null;
    private static List<HorarisEvents> listEvents = null;
    private static Map<String, Long> limitApps = new HashMap<>();
    private static List<String> blockedApps = new ArrayList<>();
    private static List<GeoFill> geoFills = new ArrayList<>();
    private static List<Oficina> oficines = new ArrayList<>();
    private static Map<Integer, String> wakeHoraris = new HashMap<>();
    private static Map<Integer, String> sleepHoraris = new HashMap<>();
    TodoApi mTodoService;

    public static boolean getGeoLocOpen() {
        return geolocOpen;
    }

    public static void setGeolocOpen(boolean b) {
        geolocOpen = b;
    }

    public static List<GeoFill> getGeoFills() {
        return geoFills;
    }

    public static void setGeoFills(List<GeoFill> list) {
        geoFills = list;
    }

    public static List<Oficina> getOficines() {
        return oficines;
    }

    public static void setOficines(List<Oficina> list) {
        oficines = list;
    }

    public static List<HorarisEvents> getListEvents() {
        return listEvents;
    }

    public static void setListEvents(List<HorarisEvents> l) {
        listEvents = l;
    }


    public static void addBlockEvent(String s) {
        blockEvents.add(s);
    }

    public static void removeBlockEvent(String s) {
        blockEvents.remove(s);
    }

    public static List<String> getBlockEvents() {
        return blockEvents;
    }

    public static Map<Integer, String> getWakeHoraris() {
        return wakeHoraris;
    }

    public static void setWakeHoraris(Map<Integer, String> m) {
        wakeHoraris = m;
    }

    public static Map<Integer, String> getSleepHoraris() {
        return sleepHoraris;
    }

    public static void setSleepHoraris(Map<Integer, String> m) {
        sleepHoraris = m;
    }

    public static long getStartFreeUse() {
        return startFreeUse;
    }

    public static void setStartFreeUse(long l) {
        startFreeUse = l;
    }

    public static long getIDChild() {
        return TodoApp.IDChild;
    }

    public static void setIDChild(long s) {
        TodoApp.IDChild = s;
    }

    public static Long getIDTutor() {
        return TodoApp.IDTutor;
    }

    public static void setIDTutor(Long l) {
        TodoApp.IDTutor = l;
    }

    public static Integer getTutor() {
        return TodoApp.tutor;
    }

    public static void setTutor(Integer i) {
        TodoApp.tutor = i;
    }

    public static int getDayOfYear() {
        return TodoApp.dayOfYear;
    }

    public static void setDayOfYear(int i) {
        TodoApp.dayOfYear = i;
    }

    public static boolean getFreeUse() {
        return TodoApp.freeUse;
    }

    public static void setFreeUse(boolean b) {
        TodoApp.freeUse = b;
    }

    public static boolean getLiveApp() {
        return TodoApp.liveApp;
    }

    public static void setLiveApp(boolean b) {
        TodoApp.liveApp = b;
    }

    public static String getCurrentAppKid() {
        return TodoApp.currentAppKid;
    }

    public static void setCurrentAppKid(String s) {
        TodoApp.currentAppKid = s;
    }

    public static String getTimeOpenedCurrentAppKid() {
        return TodoApp.timeOpenedCurrentAppKid;
    }

    public static void setTimeOpenedCurrentAppKid(String s) {
        TodoApp.timeOpenedCurrentAppKid = s;
    }

    public static Map<String, Long> getLimitApps() {
        return TodoApp.limitApps;
    }

    public static void setLimitApps(Map<String, Long> m) {
        TodoApp.limitApps = m;
    }

    public static boolean getBlockedDevice() {
        return TodoApp.blockedDevice;
    }

    public static void setBlockedDevice(boolean b) {
        TodoApp.blockedDevice = b;
    }

    public static List<String> getBlockedApps() {
        return TodoApp.blockedApps;
    }

    public static void setBlockedApps(List<String> l) {
        TodoApp.blockedApps = l;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient httpClient = getOkHttpClient(getApplicationContext());

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

    public OkHttpClient getOkHttpClient(Context context) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(readKeyStore(context));

            X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            return new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(interceptor)
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            //Evita el problema javax.net.ssl.SSLPeerUnverifiedException: Hostname not verified però no és molt segur
                            return true;
                        }
                    })
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private KeyStore readKeyStore(Context context) {

        char[] password = "adictic".toCharArray();

        try (InputStream is = context.getResources().openRawResource(R.raw.ssl_server)) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, password);
            return ks;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            e.printStackTrace();
        }

        return null;
    }
}
