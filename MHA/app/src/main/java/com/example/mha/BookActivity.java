package com.example.mha;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookActivity extends AppCompatActivity {

    private Spinner spinnerHospital, spinnerTime;
    private Button btnBook;
    private ApiService apiService;
    private List<HospitalRequest> hospitalList = new ArrayList<>();
    private List<AppointmentRequest> appointmentList = new ArrayList<>();
    private int userId;
    private int selectedHospitalId = -1;
    private AppointmentRequest selectedAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bookLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getIntExtra("UserId", -1);

        spinnerHospital = findViewById(R.id.spinnerHospital);
        spinnerTime = findViewById(R.id.spinnerTime);
        btnBook = findViewById(R.id.btnBook);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        loadHospitals();

        spinnerHospital.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (hospitalList.size() > 0) {
                    selectedHospitalId = hospitalList.get(position).hospitalID;
                    loadAvailableAppointments(selectedHospitalId);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnBook.setOnClickListener(v -> {
            if (selectedAppointment == null) {
                Toast.makeText(BookActivity.this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            attemptBooking(userId, selectedAppointment);
        });
    }

    private void loadHospitals() {
        apiService.getHospitals().enqueue(new Callback<List<HospitalRequest>>() {
            @Override
            public void onResponse(Call<List<HospitalRequest>> call, Response<List<HospitalRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hospitalList = response.body();
                    List<String> hospitalNames = new ArrayList<>();
                    for (HospitalRequest hospital : hospitalList) {
                        hospitalNames.add( CryptClass.decrypt(hospital.name) + " (" + CryptClass.decrypt(hospital.city) + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(BookActivity.this,
                            android.R.layout.simple_spinner_item, hospitalNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerHospital.setAdapter(adapter);
                } else {
                    Toast.makeText(BookActivity.this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HospitalRequest>> call, Throwable t) {
                Log.e("Booking", Objects.requireNonNull(t.getMessage()));
                Toast.makeText(BookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAvailableAppointments(int hospitalId) {
        apiService.getAppointments().enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call, Response<List<AppointmentRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> timeSlots = new ArrayList<>();
                    appointmentList.clear();

                    for (AppointmentRequest appt : response.body()) {
                        if (appt.hospitalID == hospitalId && "available".equalsIgnoreCase(appt.status)) {
                            appointmentList.add(appt);
                            timeSlots.add(CryptClass.decrypt(appt.appointmentDate) + " " + CryptClass.decrypt(appt.appointmentTime));
                        }
                    }

                    ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(BookActivity.this,
                            android.R.layout.simple_spinner_item, timeSlots);
                    timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTime.setAdapter(timeAdapter);

                    spinnerTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedAppointment = appointmentList.get(position);
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });

                } else {
                    Toast.makeText(BookActivity.this, "No available appointments found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {
                Toast.makeText(BookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Booking", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void attemptBooking(int userId, AppointmentRequest selectedAppointment) {
        apiService.getAppointmentsByUser(userId).enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call, Response<List<AppointmentRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasConflict = false;
                    String newDate = CryptClass.decrypt(selectedAppointment.appointmentDate.split("T")[0]);

                    for (AppointmentRequest appt : response.body()) {
                        String existingDate = CryptClass.decrypt(appt.appointmentDate.split("T")[0]);
                        if (existingDate.equals(newDate)) {
                            hasConflict = true;
                            break;
                        }
                    }

                    if (hasConflict) {
                        Toast.makeText(BookActivity.this,
                                "You already have an appointment on this day.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Safe to book
                        bookAppointment(selectedAppointment, userId);
                    }
                } else {
                    Toast.makeText(BookActivity.this, "Failed to check existing appointments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {
                Toast.makeText(BookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Booking", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void bookAppointment(AppointmentRequest appointment, int userId) {
        if (appointment == null) return;

        Map<String, Object> body = new HashMap<>();
        body.put("appointmentID", appointment.appointmentID);
        body.put("userID", userId);

        apiService.bookAppointment(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookActivity.this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BookActivity.this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Booking", Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}
