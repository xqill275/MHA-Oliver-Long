package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.repository.AppointmentRepository;
import com.example.mha.repository.HospitalRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewBookingActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepo;
    Button ViewBookBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_booking);

        appointmentRepo = new AppointmentRepository(this);

        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");

        if (userId != -1) {
            fetchBookings(userId);
        }

        ViewBookBack = findViewById(R.id.ViewBookBack);
        ViewBookBack.setOnClickListener(v -> {
            Intent Backintent = new Intent(ViewBookingActivity.this, ApointmentActivity.class);
            Backintent.putExtra("UserId", userId);
            Backintent.putExtra("UserRole", userRole);
            startActivity(Backintent);
        });

    }

    private void fetchBookings(int userId) {

        RecyclerView recyclerView = findViewById(R.id.recyclerBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);


        // ONLINE LOAD FIRST

        api.getAppointmentsByUser(userId).enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call,
                                   Response<List<AppointmentRequest>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<BookingAdapter.AppointmentWithId> bookingsWithId = new ArrayList<>();

                    for (AppointmentRequest ar : response.body()) {
                        bookingsWithId.add(
                                new BookingAdapter.AppointmentWithId(
                                        ar.appointmentID,
                                        ar
                                )
                        );
                    }

                    BookingAdapter adapter =
                            new BookingAdapter(ViewBookingActivity.this,
                                    bookingsWithId,
                                    userId);

                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(ViewBookingActivity.this,
                            "No bookings found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {

                new Thread(() -> {

                    List<AppointmentRequest> offlineBookings =
                            appointmentRepo.getUserAppointmentsOffline(userId);

                    HospitalRepository hospitalRepo =
                            new HospitalRepository(ViewBookingActivity.this);

                    List<BookingAdapter.AppointmentWithId> bookingsWithId =
                            new ArrayList<>();

                    for (AppointmentRequest ar : offlineBookings) {

                        // FETCH HOSPITAL DETAILS OFFLINE
                        HospitalRequest hospital =
                                hospitalRepo.getHospitalRequestById(ar.hospitalID);

                        if (hospital != null) {
                            ar.hospitalName = hospital.name;
                            ar.hospitalCity = hospital.city;
                        } else {
                            ar.hospitalName = "Unknown Hospital";
                            ar.hospitalCity = "";
                        }

                        bookingsWithId.add(
                                new BookingAdapter.AppointmentWithId(
                                        ar.appointmentID,
                                        ar
                                )
                        );
                    }

                    runOnUiThread(() -> {
                        if (bookingsWithId.isEmpty()) {
                            Toast.makeText(ViewBookingActivity.this,
                                    "No offline bookings found",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            BookingAdapter adapter =
                                    new BookingAdapter(ViewBookingActivity.this,
                                            bookingsWithId,
                                            userId);

                            recyclerView.setAdapter(adapter);
                        }
                    });

                }).start();
            }
        });
    }
}
