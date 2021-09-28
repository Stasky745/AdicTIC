package com.adictic.client.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adictic.client.BuildConfig;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.ClientNotificationManager;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Global;
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
    private ClientNotificationManager notificationManager;

    public static String[] newFeatures = {
            "Informe mensual acabat",
            "Opcions de notificacions"
    };

    public static String[] fixes = {
            "Agafa bé les dates a l'apartat d'agafar informació d'ús",
            "Ensenya temps total a l'apartat d'agafar informació d'ús",
            "Ja no entra en bucle quan surt la pàgina de bloqueig de dispositiu",
            "Ara ja es tanca bé la pàgina de bloqueig de dispositiu",
            "Ara es tanca la pàgina de bloqueig d'apps",
            "Les dades entre l'apartat d'agafar informació d'ús i la gràfica inicial ja són iguals"
    };

    public static String[] changes = {
            "Canvis en la pantalla de permisos i opcions"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        adicticApi = createRetrofit(getOkHttpClient(new AdicticAuthenticator())).create(AdicticApi.class);
        notificationManager = new ClientNotificationManager(this);
    }

    public AdicticApi getAPI() { return adicticApi; }

    public ClientNotificationManager getNotificationManager() { return notificationManager; }

    private class AdicticAuthenticator implements Authenticator {
        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, @NonNull Response response) {
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
