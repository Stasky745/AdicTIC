package com.example.adictic.rest;

import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.AppTimesAccessed;
import com.example.adictic.entity.BlockAppEntity;
import com.example.adictic.entity.BlockList;
import com.example.adictic.entity.BlockedLimitedLists;
import com.example.adictic.entity.ChangePassword;
import com.example.adictic.entity.ChatsMain;
import com.example.adictic.entity.Dubte;
import com.example.adictic.entity.EventsAPI;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.GeoFill;
import com.example.adictic.entity.HorarisAPI;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.entity.Localitzacio;
import com.example.adictic.entity.NouFillLogin;
import com.example.adictic.entity.Oficina;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserLogin;
import com.example.adictic.entity.UserMessage;
import com.example.adictic.entity.UserRegister;
import com.example.adictic.entity.VellFillLogin;
import com.example.adictic.entity.YearEntity;

import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
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

    // Si és tutor -> idChild = -1
    @POST("/users/token/{idChild}")
    Call<String> updateToken(@Path("idChild") Long idChild, @Body String token);

    @POST("/users/logout")
    Call<String> logout(@Body String token);

    @POST("/users/check")
    Call<User> checkWithToken(@Body String token);

    @POST("/users/register")
    Call<String> register(@Body UserRegister register);

    @POST("/users/{id}/child")
    Call<String> sendOldName(@Path("id") Long id, @Body VellFillLogin fill);

    @PUT("/users/{id}/child")
    Call<Long> sendNewName(@Path("id") Long id, @Body NouFillLogin fill);

    @GET("/usage/{id}/{xDays}")
    Call<Collection<GeneralUsage>> getAppUsage(@Path("id") Long childId, @Path("xDays") Integer xDays);

    /**
     * format {dd-mm-aaaa} o {mm-aaaa} per tot el mes
     **/
    @GET("/usage/{id}/{dataInicial}/{dataFinal}")
    Call<Collection<GeneralUsage>> getGenericAppUsage(@Path("id") Long childId, @Path("dataInicial") String dataInicial, @Path("dataFinal") String dataFinal);

    @POST("/usage/{id}")
    Call<String> sendAppUsage(@Path("id") Long childId, @Body Collection<GeneralUsage> appUsage);

    @GET("/users/{id}/blockedLists")
    Call<BlockedLimitedLists> getBlockedLimitedLists(@Path("id") Long childId);

    @GET("/users/{id}/child")
    Call<Collection<FillNom>> getUserChilds(@Path("id") Long userId);

    @POST("/users/{id}/callBlockedApp")
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

    @GET("/users/{idChild}/blockStatus")
    Call<Boolean> getBlockStatus(@Path("idChild") Long childId);

    @POST("/usage/{id}/horaris")
    Call<String> postHoraris(@Path("id") Long childId, @Body HorarisAPI horaris);

    @GET("/usage/{id}/horaris")
    Call<HorarisAPI> getHoraris(@Path("id") Long childId);

    @POST("/usage/{id}/events")
    Call<String> postEvents(@Path("id") Long childId, @Body EventsAPI horaris);

    @GET("/usage/{id}/events")
    Call<EventsAPI> getEvents(@Path("id") Long childId);

    @GET("/usage/{id}/horarisEvents")
    Call<HorarisEvents> getHorarisEvents(@Path("id") Long childId);

    @POST("/icons/{pkgName}")
    @Multipart
    Call<String> postIcon(@Path("pkgName") String pkgName, @Part MultipartBody.Part file);

    @GET("/icons/{pkgName}")
    Call<ResponseBody> getIcon(@Path("pkgName") String pkgName);

    @GET("/usage/{idChild}/blockedApps")
    Call<Collection<BlockAppEntity>> getBlockApps(@Path("idChild") Long childId);

    @POST("/usage/{idChild}/limitedApps")
    Call<String> limitApps(@Path("idChild") Long childId, @Body BlockList bList);

    @POST("/usage/{idChild}/blockedApps")
    Call<String> blockApps(@Path("idChild") Long childId, @Body List<String> bList);

    @POST("/usage/{idChild}/unlockApps")
    Call<String> unlockApps(@Path("idChild") Long childId, @Body List<String> bList);

    @GET("/users/{idChild}/age")
    Call<Integer> getAge(@Path("idChild") Long idChild);

    @GET("/usage/{idChild}/timesTried")
    Call<List<AppTimesAccessed>> getAccessBlocked(@Path("idChild") Long childId);

    @GET("/offices")
    Call<List<Oficina>> getOficines();

    @GET("/users/geoloc")
    Call<List<GeoFill>> getGeoLoc();

    @POST("/users/geoloc")
    Call<String> postGeolocActive(@Body Boolean b);

    @POST("/users/geoloc/{idChild}")
    Call<String> postCurrentLocation(@Path("idChild") Long idChild, @Body GeoFill geoFill);

    @GET("/poblacions")
    Call<Collection<Localitzacio>> getLocalitzacions();

    @POST("/users/dubtes")
    Call<String> postDubte(@Body Dubte dubte);

    ////////////////////////////////////
    //Chat
    ///////////////////////////////////

    @POST("/message/access")
    Call<String> giveAccess(@Body Boolean access);

    @GET("/message/me/info")
    Call<ChatsMain> getChatsInfo();

    @POST("/message/me/{id}/close")
    Call<String> closeChat(@Path("id") Long idUserAdmin);

    @GET("/message/me/{id}")
    Call<List<UserMessage>> getMyMessagesWithUser(@Path("id") String id);

    @POST("/message/me/{id}")
    Call<String> sendMessageToUser(@Path("id") String id, @Body UserMessage value);

    ///////////////////////////////////

    @GET("/admins/pictures/{id}")
    Call<ResponseBody> getAdminPicture(@Path("id") Long id);

    @GET("/usage/{idChild}/lastAppUsed")
    Call<LiveApp> getLastAppUsed(@Path("idChild") Long idChild);

    @POST("/usage/{idChild}/lastAppUsed")
    Call<String> postLastAppUsed(@Path("idChild") Long idChild, @Body LiveApp liveApp);

    @POST("/users/changePassword")
    Call<String> changePassword(@Body ChangePassword cp);

    @POST("/usage/{idChild}/installedApp")
    Call<String> postAppInstalled(@Path("idChild") Long idChild, @Body AppInfo appInfo);

    @POST("/usage/{idChild}/uninstalledApp")
    Call<String> postAppUninstalled(@Path("idChild") Long idChild, @Body String pkgName);
}
