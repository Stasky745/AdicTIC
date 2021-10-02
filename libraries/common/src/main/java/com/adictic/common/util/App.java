package com.adictic.common.util;

import android.annotation.SuppressLint;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
    
    protected OkHttpClient getCertOkHttpClient(Authenticator authenticator){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(getAssets().open("ca_bundle.crt"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                httpClient.addInterceptor(interceptor);
            }

            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

            return httpClient
                    .cookieJar(cookieJar)
                    .authenticator(authenticator)
                    .sslSocketFactory(context.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                    .build();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected OkHttpClient getUnsafeOkHttpClient(Authenticator authenticator) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            //okhttp3.Credentials.basic("username", "password");
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            if(BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                builder.addInterceptor(interceptor);
            }

            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            //builder.addInterceptor(new BasicAuthInterceptor("username", "password"));
            builder.hostnameVerifier((hostname, session) -> true);
            builder.cookieJar(cookieJar);
            builder.authenticator(authenticator);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
