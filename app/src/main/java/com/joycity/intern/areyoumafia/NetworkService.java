package com.joycity.intern.areyoumafia;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NetworkService {
    @Headers({"Content-Type: application/json"})
    @POST("signup")
    Call<Map<String,Object>> signUp(@Body User user);

    @POST("login")
    Call<Map<String,Object>> login(@Body User user);

    @GET("rooms")
    Call<Map<String,Object>> getRooms(@Header("JSESSION") String sessionkey);


    @POST("rooms")
    Call<Map<String,Object>> postRooms(@Header("JSESSION") String sessionkey);

    @POST("rooms/{room_id}")
    Call<Map<String,Object>> getRoom(@Header("JSESSION") String sessionkey, @Path("room_id") int room_id);


    @POST("matching")
    Call<Map<String, Object>> requestMatching(@Header("JSESSION") String sessionkey);
}
