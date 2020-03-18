package com.example.adictic.rest;

import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.entity.UserRegister;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TodoApi {
    @POST("/users/login")
    Call<User> login(@Body UserLogin login);

    @GET("/users/check")
    Call<String> check();

    @POST("/users/register")
    Call<User> register(@Body UserRegister register);

    @POST("/users/{id}/name")
    Call<String> sendName(@Body String name);
}
