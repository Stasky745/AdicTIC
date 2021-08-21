package com.example.adictic.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Global;
import com.example.adictic.BuildConfig;
import com.example.adictic.rest.AdicticApi;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class AdicticApp extends App {

    private AdicticApi adicticApi;

    public static String[] newFeatures = {

    };

    public static String[] fixes = {
            "No apareixen apps que no arriben a 1 minut a l'ús del dia"
    };

    public static String[] changes = {
            "L'app és en castellà de base ara",
            "Nova pantalla de permisos",
            "Nova pantalla d'informe mensual"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        adicticApi = createRetrofit(getOkHttpClient(new AdicticAuthenticator())).create(AdicticApi.class);
    }

    public AdicticApi getAPI() {
        return adicticApi;
    }

    private class AdicticAuthenticator implements Authenticator {
        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
            if (responseCount(response) >= 3) {
                return null; // If we've failed 3 times, give up.
            }

            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

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
        }
    }
}
