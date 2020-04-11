package com.example.adictic.rest;

import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.NouFillLogin;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.entity.UserRegister;
import com.example.adictic.entity.VellFillLogin;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TodoApi {
    @POST("/users/login")
    Call<User> login(@Body UserLogin login);

    @POST("/users/logout")
    Call<String> logout();

    @GET("/users/check")
    Call<String> check();

    @POST("/users/register")
    Call<User> register(@Body UserRegister register);

    @POST("/users/{id}/child")
    Call<String> sendOldName(@Path("id") Long id, @Body VellFillLogin fill);

    @PUT("/users/{id}/child")
    Call<String> sendNewName(@Path("id") Long id, @Body NouFillLogin fill);

    @GET("/usage/{id}/{xDays}")
    Call<Collection<GeneralUsage>> getAppUsage(@Path("id") Long childId, @Path("xDays") Integer xDays);

    @POST("/usage/{id}")
    Call<String> sendAppUsage(@Path("id") Long childId, @Body Collection<GeneralUsage> appUsage);
}
