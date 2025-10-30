package com.example.mha.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @GET("api/users")
    Call<List<UserRequest>> getUsers();

    @POST("api/users")
    Call<Void> addUser(@Body UserRequest user);

    @PUT("api/user/role")
    Call<Void> updateUserRole(@Body Map<String, Object> body);

    // hospitals

    @POST("api/hospitals")
    Call<Void> addHospital(@Body HospitalRequest hospital);

    @GET("api/hospitals")
    Call<List<HospitalRequest>> getHospitals();

    @POST("api/appointments/add")
    Call<Void> addAppointment(@Body AppointmentRequest appointment);

}