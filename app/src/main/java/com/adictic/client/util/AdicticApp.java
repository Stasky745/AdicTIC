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
            "Historial de notificacions a l'apartat d'opcions",
            "El tutor rep notificació que s'ha desactivat el permís d'administrador quan això passa",
            "El tutor rep notificació quan s'ha desconnectat el servei d'accessibilitat",
            "Ara es pot desbloquejar el dispositiu des del dispositiu fill amb la contrasenya",
            "Entrar al dispositiu tutor amb contrasenya o emprempta (des d'opcions)",
            "Text de la pantalla de bloqueig canvia depenent de la raó de bloqueig"
    };

    public static String[] fixes = {

    };

    public static String[] changes = {

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
