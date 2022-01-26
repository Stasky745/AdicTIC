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
import com.adictic.common.util.hilt.Repository;
import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.HiltAndroidApp;
import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

@HiltAndroidApp
public class AdicticApp extends App {
    public static String[] newFeatures = {
            "Historial de notificacions a l'apartat d'opcions",
            "El tutor rep notificació que s'ha desactivat el permís d'administrador quan això passa",
            "El tutor rep notificació quan s'ha desconnectat el servei d'accessibilitat",
            "Ara es pot desbloquejar el dispositiu des del dispositiu fill amb la contrasenya",
            "Entrar al dispositiu tutor amb contrasenya o emprempta (des d'opcions)",
            "Text de la pantalla de bloqueig canvia depenent de la raó de bloqueig",
            "Nova pantalla de notificacions a l'apartat de configuració",
            "Edició de nom i data naixement del fill"
    };

    public static String[] fixes = {

    };

    public static String[] changes = {

    };
}
