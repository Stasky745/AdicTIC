package com.adictic.common.rest;

import com.adictic.common.entity.AppTimesAccessed;
import com.adictic.common.entity.BlockAppEntity;
import com.adictic.common.entity.BlockInfo;
import com.adictic.common.entity.BlockList;
import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.CanvisAppBlock;
import com.adictic.common.entity.CanvisEvents;
import com.adictic.common.entity.CanvisHoraris;
import com.adictic.common.entity.ChangePassword;
import com.adictic.common.entity.ChatsMain;
import com.adictic.common.entity.EventsAPI;
import com.adictic.common.entity.FillNom;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.GeoFill;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisEvents;
import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.entity.LiveApp;
import com.adictic.common.entity.Localitzacio;
import com.adictic.common.entity.Oficina;
import com.adictic.common.entity.TimeBlock;
import com.adictic.common.entity.TimeFreeUse;
import com.adictic.common.entity.User;
import com.adictic.common.entity.UserMessage;
import com.adictic.common.entity.YearEntity;

import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {
    // Si és tutor -> idChild = -1
    @POST("/users/token/{idChild}")
    Call<String> updateToken(@Path("idChild") Long idChild, @Body String token);

    @POST("/users/logout")
    Call<String> logout(@Body String token);

    @POST("/users/check")
    Call<User> checkWithToken(@Body String token);

    @GET("/usage/{id}/{xDays}")
    Call<Collection<GeneralUsage>> getAppUsage(@Path("id") Long childId, @Path("xDays") Integer xDays);

    @POST("/usage/{id}/events")
    Call<String> postEvents(@Path("id") Long childId, @Body EventsAPI horaris);

    @POST("/usage/{id}/horaris")
    Call<String> postHoraris(@Path("id") Long childId, @Body HorarisAPI horaris);

    @POST("/usage/{idChild}/blockedApps")
    Call<String> blockApps(@Path("idChild") Long childId, @Body List<String> bList);

    @POST("/usage/{idChild}/unlockApps")
    Call<String> unlockApps(@Path("idChild") Long childId, @Body List<String> bList);

    @POST("/usage/{idChild}/limitedApps")
    Call<String> limitApps(@Path("idChild") Long childId, @Body BlockList bList);

    /**
     * format {dd-mm-aaaa} o {mm-aaaa} per tot el mes
     **/
    @GET("/usage/{id}/{dataInicial}/{dataFinal}")
    Call<Collection<GeneralUsage>> getGenericAppUsage(@Path("id") Long childId, @Path("dataInicial") String dataInicial, @Path("dataFinal") String dataFinal);

    @GET("/users/{id}/blockedLists")
    Call<BlockedLimitedLists> getBlockedLimitedLists(@Path("id") Long childId);

    @GET("/users/{id}/child")
    Call<Collection<FillNom>> getUserChilds(@Path("id") Long userId);

    @GET("/users/{idTutor}/{idChild}")
    Call<Collection<FillNom>> getChildInfo(@Path("idTutor") Long idTutor, @Path("idChild") Long idChild);

    @GET("/usage/{id}/daysUsage")
    Call<List<YearEntity>> getDaysWithData(@Path("id") Long childId);

    @GET("/usage/{id}/horaris")
    Call<HorarisAPI> getHoraris(@Path("id") Long childId);

    @GET("/usage/{id}/events")
    Call<EventsAPI> getEvents(@Path("id") Long childId);

    @GET("/usage/{id}/horarisEvents")
    Call<HorarisEvents> getHorarisEvents(@Path("id") Long childId);

    @GET("/icons/{pkgName}")
    Call<ResponseBody> getIcon(@Path("pkgName") String pkgName);

    @GET("/usage/{idChild}/blockedApps")
    Call<Collection<BlockAppEntity>> getBlockApps(@Path("idChild") Long childId);

    @GET("/users/{idChild}/age")
    Call<Integer> getAge(@Path("idChild") Long idChild);

    @GET("/usage/{idChild}/timesTried")
    Call<List<AppTimesAccessed>> getAccessBlocked(@Path("idChild") Long childId);

    @POST("/users/{id}/block")
    Call<String> blockChild(@Path("id") Long childId);

    @POST("/users/{id}/unblock")
    Call<String> unblockChild(@Path("id") Long childId);

    @POST("/users/{idChild}/freeuse")
    Call<String> freeUse(@Path("idChild") Long childId, @Body Boolean freeUse);

    @GET("/offices")
    Call<List<Oficina>> getOficines();

    @GET("/users/geoloc")
    Call<List<GeoFill>> getGeoLoc();

    @POST("/usage/{id}/liveApp")
    Call<String> askChildForLiveApp(@Path("id") Long childId, @Body Boolean liveApp);

    @GET("/poblacions")
    Call<Collection<Localitzacio>> getLocalitzacions();

    ////////////////////////////////////
    //Chat
    ///////////////////////////////////

    @GET("/message/client/{childId}/info")
    Call<ChatsMain> getChatsInfo(@Path("childId") Long childId);

    @POST("/message/client/{childId}/{adminId}/close")
    Call<String> closeChat(@Path("adminId") Long idUserAdmin, @Path("childId") Long idChild);

    @GET("/message/client/{childId}/{adminId}")
    Call<List<UserMessage>> getMyMessagesWithUser(@Path("childId") Long childId, @Path("adminId") Long adminId);

    @POST("/message/client/{childId}/{adminId}")
    Call<String> sendMessageToUser(@Path("childId") Long childId, @Path("adminId") Long adminId, @Body UserMessage value);

    ///////////////////////////////////

    @GET("/admins/pictures/{id}")
    Call<ResponseBody> getAdminPicture(@Path("id") Long id);

    @GET("/usage/{idChild}/lastAppUsed")
    Call<LiveApp> getLastAppUsed(@Path("idChild") Long idChild);

    @POST("/users/changePassword")
    Call<String> changePassword(@Body ChangePassword cp);

    @POST("/update/adictic")
    Call<String> checkForUpdates(@Body String version);

    @GET("/update/adictic")
    Call<ResponseBody> getLatestVersion();

    @GET("/usage/{id}/events/{data}")
    Call<Collection<CanvisEvents>> getCanvisEvents(@Path("id") Long id, @Path("data") String data);

    @GET("/usage/{id}/horaris/{data}")
    Call<Collection<CanvisHoraris>> getCanvisHoraris(@Path("id") Long id, @Path("data") String data);

    @GET("/usage/{id}/blockedApps/{data}")
    Call<Collection<CanvisAppBlock>> getCanvisApps(@Path("id") Long id, @Path("data") String data);

    @GET("/usage/{idChild}/accessInfo/{data}")
    Call<BlockInfo> getAccessInfo(@Path("idChild") Long idChild, @Path("data") String data);

    @POST("/usage/{idChild}/tempsFreeuse")
    Call<String> postTempsFreeUse(@Path("idChild") Long idChild, @Body TimeFreeUse timeFreeUse);

    @POST("/usage/{idChild}/tempsBloqueig")
    Call<String> postTempsBloqueig(@Path("idChild") Long idChild, @Body TimeBlock timeBlock);

    @POST("/usage/{idChild}/intentAccesApp")
    Call<String> postIntentAccesApp(@Path("idChild") Long idChild, @Body IntentsAccesApp intentsAccesApp);

    @POST("/usage/{idChild}/intentAccesDisp")
    Call<String> postIntentAccesDisp(@Path("idChild") Long idChild, @Body Long data);

    /////////////// ACRA ///////////////

    @POST("/crash/{appName}/{version}")
    Call<String> sendCrashACRA(@Path("appName") String appName, @Path("version") String version, @Body String json);

    ///////////////////////////////////

    /////////////// VIDEOCALL //////////////

    @GET("/message/videochat/cancel/{otherUserId}/{childId}")
    Call<String> cancelCallToUser(@Path("otherUserId") Long otherUserId, @Path("childId") Long childId);

    @POST("/message/videochat/answer/{adminId}")
    Call<String> answerCallOfAdmin(@Path("adminId") Long adminId, @Body String answer);

    ///////////////////////////////////////
}
