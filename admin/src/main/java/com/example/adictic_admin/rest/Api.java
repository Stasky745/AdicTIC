package com.example.adictic_admin.rest;

import com.example.adictic_admin.entity.AdminLogin;
import com.example.adictic_admin.entity.AdminProfile;
import com.example.adictic_admin.entity.AppTimesAccessed;
import com.example.adictic_admin.entity.BlockAppEntity;
import com.example.adictic_admin.entity.ChatInfo;
import com.example.adictic_admin.entity.ChatsMain;
import com.example.adictic_admin.entity.Dubte;
import com.example.adictic_admin.entity.EventsAPI;
import com.example.adictic_admin.entity.FillNom;
import com.example.adictic_admin.entity.GeneralUsage;
import com.example.adictic_admin.entity.HorarisAPI;
import com.example.adictic_admin.entity.Oficina;
import com.example.adictic_admin.entity.OficinaNova;
import com.example.adictic_admin.entity.UserLogin;
import com.example.adictic_admin.entity.UserMessage;
import com.example.adictic_admin.entity.YearEntity;

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

public interface Api {

    @POST("/users/loginAdmin")
    Call<AdminLogin> login(@Body UserLogin login);

    @POST("/users/token/{idChild}")
    Call<String> updateToken(@Path("idChild") Long idChild, @Body String token);

    @POST("/users/logout")
    Call<String> logout(@Body String token);

    @GET("/admins/{id}/profile")
    Call<AdminProfile> getProfile(@Path("id") Long id);

    @POST("admins/{id}/profile")
    Call<String> postProfile(@Path("id") Long id, @Body AdminProfile adminProfile);

    @POST("/admins/pictures/{id}")
    @Multipart
    Call<String> postPicture(@Path("id") Long id, @Part MultipartBody.Part file);

    @GET("/admins/pictures/{id}")
    Call<ResponseBody> getPicture(@Path("id") Long id);

    @POST("/offices")
    Call<String> actualitzarOficina(@Body Oficina oficina);

    @PUT("/offices")
    Call<Long> crearOficina(@Body OficinaNova oficina);

    @GET("/admins/dubtes")
    Call<List<Dubte>> getDubtes();

    @POST("/admins/dubte/{idDubte}")
    Call<ChatInfo> getUserChatInfo(@Path("idDubte") Long idDubte);

    @POST("/message/admin/{userId}/{childId}/close")
    Call<String> closeChat(@Path("userId") Long userId, @Path("childId") Long childId);

    @GET("/message/admin/{clientId}/{childId}")
    Call<List<UserMessage>> getMyMessagesWithUser(@Path("clientId") Long clientId, @Path("childId") Long childId);

    @POST("/message/admin/{userId}/{childId}")
    Call<String> sendMessageToUser(@Path("userId") Long userId, @Path("childId") Long childId, @Body UserMessage value);

    @GET("/admins/chats")
    Call<ChatsMain> getAllChats();

    @GET("/users/{id}/child")
    Call<Collection<FillNom>> getUserChilds(@Path("id") Long userId);

    @GET("/users/{idTutor}/{idChild}")
    Call<Collection<FillNom>> getChildInfo(@Path("idTutor") Long idTutor, @Path("idChild") Long idChild);

    /**
     * format {dd-mm-aaaa} o {mm-aaaa} per tot el mes
     **/
    @GET("/usage/{id}/{dataInicial}/{dataFinal}")
    Call<Collection<GeneralUsage>> getGenericAppUsage(@Path("id") Long childId, @Path("dataInicial") String dataInicial, @Path("dataFinal") String dataFinal);

    @GET("/usage/{idChild}/blockedApps")
    Call<Collection<BlockAppEntity>> getBlockApps(@Path("idChild") Long childId);

    @GET("/users/{idChild}/age")
    Call<Integer> getAge(@Path("idChild") Long idChild);

    @GET("/usage/{idChild}/timesTried")
    Call<List<AppTimesAccessed>> getAccessBlocked(@Path("idChild") Long childId);

    @GET("/usage/{id}/daysUsage")
    Call<List<YearEntity>> getDaysWithData(@Path("id") Long childId);

    @GET("/usage/{id}/events")
    Call<EventsAPI> getEvents(@Path("id") Long childId);

    @GET("/usage/{id}/horaris")
    Call<HorarisAPI> getHoraris(@Path("id") Long childId);

    @POST("/users/check")
    Call<AdminLogin> checkWithToken(@Body String token);

    @GET("/offices")
    Call<List<Oficina>> getOficines();

    @POST("/update/adictic/admin")
    Call<String> checkForUpdates(@Body String version);

    @GET("/update/adictic/admin")
    Call<ResponseBody> getLatestVersion();

    @GET("/message/videochat/{otherUserId}/{childId}")
    Call<String> callOtherUser(@Path("otherUserId") Long otherUserId, @Path("childId") Long childId);
}
