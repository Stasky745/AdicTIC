package com.example.adictic.rest;

import com.example.adictic.entity.BlockAppEntity;
import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.entity.NouFillLogin;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.entity.UserRegister;
import com.example.adictic.entity.VellFillLogin;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.entity.YearEntity;

import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface TodoApi {
    @POST("/users/login")
    Call<User> login(@Body UserLogin login);

    @POST("/users/logout")
    Call<String> logout();

    @GET("/users/check")
    Call<User> check();

    @POST("/users/register")
    Call<String> register(@Body UserRegister register);

    @POST("/users/{id}/child")
    Call<String> sendOldName(@Path("id") Long id, @Body VellFillLogin fill);

    @PUT("/users/{id}/child")
    Call<Long> sendNewName(@Path("id") Long id, @Body NouFillLogin fill);

    @GET("/usage/{id}/{xDays}")
    Call<Collection<GeneralUsage>> getAppUsage(@Path("id") Long childId, @Path("xDays") Integer xDays);

    /** format {dd-mm-aaaa} o {mm-aaaa} per tot el mes**/
    @GET("/usage/{id}/{dataInicial}/{dataFinal}")
    Call<Collection<GeneralUsage>> getGenericAppUsage(@Path("id") Long childId, @Path("dataInicial") String dataInicial, @Path("dataFinal") String dataFinal);

    @POST("/usage/{id}")
    Call<String> sendAppUsage(@Path("id") Long childId, @Body Collection<GeneralUsage> appUsage);

    @GET("/users/{id}/blockedLists")
    Call<BlockedLimitedLists> getBlockedLimitedLists(@Path("id") Long childId);

    @GET("/users/{id}/child")
    Call<Collection<FillNom>> getUserChilds(@Path("id") Long userId);

    @POST("/users/{idChild}/callBlockedApp")
    Call<String> callBlockedApp(@Path("id") Long childId, @Body String packageName);

    @GET("/usage/{id}/daysUsage")
    Call<List<YearEntity>> getDaysWithData(@Path("id") Long childId);

    @POST("/usage/{id}/installedApps")
    Call<String> postInstalledApps(@Path("id") Long childId, @Body Collection<AppInfo> appInfos);

    @GET("/usage/{idChild}/installedApps")
    Call<Collection<AppInfo>> getInstalledApps(@Path("id") Long childId);

    @POST("/usage/{id}/liveApp")
    Call<String> askChildForLiveApp(@Path("id") Long childId, @Body Boolean liveApp);

    @PUT("/usage/{id}/liveApp")
    Call<String> sendTutorLiveApp(@Path("id") Long childId, @Body LiveApp login);

    @POST("/users/{id}/block")
    Call<String> blockChild(@Path("id") Long childId);

    @POST("/users/{id}/unblock")
    Call<String> unblockChild(@Path("id") Long childId);

    @POST("/users/{id}/horaris")
    Call<String> postHoraris(@Path("id") Long childId, @Body WakeSleepLists wakeSleepLists);

    @GET("/users/{id}/horaris")
    Call<WakeSleepLists> getHoraris(@Path("id") Long childId);

    @POST("/icons/{pkgName}")
    @Multipart
    Call<String> postIcon(@Path("pkgName") String pkgName, @Part MultipartBody.Part file);

    @GET("/usage/{idChild}/blockedApps")
    Call<Collection<BlockAppEntity>> getBlockApps(@Path("idChild") Long childId);

}
