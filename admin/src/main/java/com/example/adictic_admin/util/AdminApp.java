package com.example.adictic_admin.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Global;
import com.example.adictic_admin.BuildConfig;
import com.example.adictic_admin.rest.AdminApi;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class AdminApp extends App {

    private AdminApi adminApi;

    public static String[] newFeatures = {
            "Es pot començar videotrucada des del xat"
    };

    public static String[] fixes = {
            "Ja no peta quan es miren els horaris nocturns del fill",
            "El percentatge de l'informe funciona bé"
    };

    public static String[] changes = {

    };

    @Override
    public void onCreate() {
        super.onCreate();
        adminApi = createRetrofit(getOkHttpClient(new AdminAuthenticator())).create(AdminApi.class);
    }

    public AdminApi getAPI() {
        return adminApi;
    }

    private class AdminAuthenticator implements Authenticator {
        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
            if (responseCount(response) >= 3) {
                return null; // If we've failed 3 times, give up.
            }

            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

            if(sharedPreferences == null)
                return null;

            String username = sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME,null);
            String password = sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD, null);

            if(username != null && password != null) {
                System.out.println("Authenticating for response: " + response);
                System.out.println("Challenges: " + response.challenges());

                UserLogin userLogin = new UserLogin();
                userLogin.username = username;
                userLogin.password = password;
                userLogin.token = sharedPreferences.getString(Constants.SHARED_PREFS_TOKEN, "");

                String gson = new Gson().toJson(userLogin);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(gson, JSON);

                String url = BuildConfig.DEBUG ? Global.BASE_URL_DEBUG : Global.BASE_URL_RELEASE;
                url += "users/loginAdmin";

                return response.request().newBuilder()
                        .url(url)
                        .post(body)
                        .build();
            }

            return null;
        }
    }
}
