package com.example.mha.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/users")
    Call<List<UserRequest>> getUsers();

    @POST("api/users")
    Call<Void> addUser(@Body UserRequest user);
}