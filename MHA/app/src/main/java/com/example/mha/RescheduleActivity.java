package com.example.mha;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RescheduleActivity extends AppCompatActivity {


    private TextView tvCurrentAppointment;
    private Spinner spinnerNewTime;
    private Button btnConfirmReschedule;
    private ApiService apiService;

    private int userId, oldAppointmentId, hospitalId;
    private String oldDate, oldTime;

    private List<AppointmentRequest> availableAppointments = new ArrayList<>();
    private AppointmentRequest selectedAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reschedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 UI references
        tvCurrentAppointment = findViewById(R.id.tvCurrentAppointment);
        spinnerNewTime = findViewById(R.id.spinnerNewTime);
        btnConfirmReschedule = findViewById(R.id.btnConfirmReschedule);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 🔹 Get data from intent
        oldAppointmentId = getIntent().getIntExtra("AppointmentID", -1);
        hospitalId = getIntent().getIntExtra("HospitalID", -1);
        userId = getIntent().getIntExtra("UserID", -1);
        oldDate = getIntent().getStringExtra("OldDate");
        oldTime = getIntent().getStringExtra("OldTime");

        // 🔹 Display current appointment
        tvCurrentAppointment.setText("Current: " + oldDate + " at " + oldTime);

        // 🔹 Load available new time slots for the same hospital
        loadAvailableAppointments();

        // 🔹 When confirm is clicked
        btnConfirmReschedule.setOnClickListener(v -> {
            if (selectedAppointment == null) {
                Toast.makeText(this, "Please select a new time.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cancel old appointment first, then book new one
            cancelAndBook(selectedAppointment);
        });
    }

    private void loadAvailableAppointments() {
        apiService.getAppointments().enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call, Response<List<AppointmentRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableAppointments.clear();
                    List<String> timeSlots = new ArrayList<>();

                    for (AppointmentRequest appt : response.body()) {
                        if (appt.hospitalID == hospitalId && "available".equalsIgnoreCase(appt.status)) {
                            availableAppointments.add(appt);
                            timeSlots.add(appt.appointmentDate + " " + appt.appointmentTime);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RescheduleActivity.this,
                            android.R.layout.simple_spinner_item,
                            timeSlots
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerNewTime.setAdapter(adapter);

                    spinnerNewTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedAppointment = availableAppointments.get(position);
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                            selectedAppointment = null;
                        }
                    });

                } else {
                    Toast.makeText(RescheduleActivity.this, "No available appointments found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {
                Toast.makeText(RescheduleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelAndBook(AppointmentRequest newAppointment) {
        // Step 1: Cancel old appointment
        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("appointmentID", oldAppointmentId);

        apiService.cancelAppointment(cancelBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> cancelResponse) {
                if (cancelResponse.isSuccessful()) {
                    // Step 2: Book the new one
                    Map<String, Object> bookBody = new HashMap<>();
                    bookBody.put("appointmentID", newAppointment.appointmentID); // update this if AppointmentRequest gets appointmentID
                    bookBody.put("userID", userId);

                    apiService.bookAppointment(bookBody).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> bookResponse) {
                            if (bookResponse.isSuccessful()) {
                                Toast.makeText(RescheduleActivity.this, "Appointment rescheduled!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RescheduleActivity.this, "Failed to book new appointment.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RescheduleActivity.this, "Error booking new: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(RescheduleActivity.this, "Failed to cancel old appointment.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RescheduleActivity.this, "Error canceling: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
