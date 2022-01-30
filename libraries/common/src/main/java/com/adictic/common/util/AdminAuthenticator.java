package com.adictic.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adictic.common.BuildConfig;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.hilt.Repository;
import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.hilt.EntryPoints;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class AdminAuthenticator implements Authenticator {

    Repository repository;

    public AdminAuthenticator(Context context) {
        super();

        HiltEntryPoint mEntryPoint = EntryPoints.get(context, HiltEntryPoint.class);
        repository = mEntryPoint.getRepository();
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) {
        if (responseCount(response) >= 3) {
            return null; // If we've failed 3 times, give up.
        }

        SharedPreferences sharedPreferences = repository.getEncryptedSharedPreferences();

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

    protected int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
