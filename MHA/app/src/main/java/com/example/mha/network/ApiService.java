package com.example.mha.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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

    @GET("api/hospitals/{id}")
    Call<HospitalRequest> getHospitalById(@Path("id") int id);

    @POST("api/appointments/add")
    Call<Void> addAppointment(@Body AppointmentRequest appointment);

    @GET("api/appointments")
    Call<List<AppointmentRequest>> getAppointments();

    @GET("api/appointments/user/{userId}")
    Call<List<AppointmentRequest>> getAppointmentsByUser(@Path("userId") int userId);

    @POST("api/appointments/cancel")
    Call<Void> cancelAppointment(@Body Map<String, Object> body);

    // Book an appointment (requires appointmentID + userID in body)
    @POST("api/appointments/book")
    Call<Void> bookAppointment(@Body Map<String, Object> body);

    @GET("/api/records/{userID}")
    Call<RecordRequest> getRecord(@Path("userID") int userID);

    @POST("/api/records/update")
    Call<RecordRequest> updateRecord(@Body RecordRequest record);

    @GET("api/vitals/{userID}")
    Call<VitalsRequest> getVitals(@Path("userID") int userID);

    @POST("api/vitals/update")
    Call<VitalsRequest> updateVitals(@Body VitalsRequest vitals);

}