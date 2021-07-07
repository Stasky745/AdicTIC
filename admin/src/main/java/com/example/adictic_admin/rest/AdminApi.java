package com.example.adictic_admin.rest;

import com.adictic.common.entity.AdminProfile;
import com.adictic.common.entity.Dubte;
import com.adictic.common.entity.Oficina;
import com.adictic.common.entity.User;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.entity.UserMessage;
import com.adictic.common.rest.Api;
import com.example.adictic_admin.entity.ChatInfo;
import com.example.adictic_admin.entity.ChatsMain;

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

public interface AdminApi extends Api {
    @POST("/users/loginAdmin")
    Call<User> login(@Body UserLogin login);

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
    Call<Long> crearOficina(@Body Oficina oficina);

    @GET("/admins/dubtes")
    Call<List<Dubte>> getDubtes();

    @POST("/admins/dubte/{idDubte}")
    Call<ChatInfo> getUserChatInfo(@Path("idDubte") Long idDubte);

    @POST("/message/admin/{userId}/{childId}/close")
    Call<String> closeChat(@Path("userId") Long userId, @Path("childId") Long childId);

    @POST("/message/admin/{userId}/{childId}")
    Call<String> sendMessageToUser(@Path("userId") Long userId, @Path("childId") Long childId, @Body UserMessage value);

    @GET("/admins/chats")
    Call<ChatsMain> getAllChats();

    @GET("/users/{idChild}/age")
    Call<Integer> getAge(@Path("idChild") Long idChild);

    @POST("/update/adictic/admin")
    Call<String> checkForUpdates(@Body String version);

    @GET("/update/adictic/admin")
    Call<ResponseBody> getLatestVersion();

    @GET("/message/videochat/{otherUserId}/{childId}")
    Call<String> callOtherUser(@Path("otherUserId") Long otherUserId, @Path("childId") Long childId);
}
