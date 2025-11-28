package com.example.mha;

import android.content.Intent;
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
import com.example.mha.repository.AppointmentRepository;

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
    private Button btnConfirmReschedule, btnRescheduleBack;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RecordsBack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");



        // UI references
        tvCurrentAppointment = findViewById(R.id.tvCurrentAppointment);
        spinnerNewTime = findViewById(R.id.spinnerNewTime);
        btnConfirmReschedule = findViewById(R.id.btnConfirmReschedule);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        //  Get data from intent
        oldAppointmentId = getIntent().getIntExtra("AppointmentID", -1);
        hospitalId = getIntent().getIntExtra("HospitalID", -1);
        oldDate = CryptClass.decrypt(getIntent().getStringExtra("OldDate"));
        oldTime = CryptClass.decrypt(getIntent().getStringExtra("OldTime"));

        btnRescheduleBack = findViewById(R.id.RescheduleBack);


        //  Display current appointment
        tvCurrentAppointment.setText("Current: " + oldDate + " at " + oldTime);

        //  Load available new time slots for the same hospital
        loadAvailableAppointments();

        btnRescheduleBack.setOnClickListener(v -> {
            Intent Backintent = new Intent(RescheduleActivity.this, ViewBookingActivity.class);
            Backintent.putExtra("UserId", userId);
            Backintent.putExtra("UserRole", userRole);
            startActivity(Backintent);

        });

        //  When confirm is clicked
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
                        if (appt.hospitalID == hospitalId &&
                                "available".equalsIgnoreCase(appt.status)) {

                            availableAppointments.add(appt);

                            timeSlots.add(
                                    CryptClass.decrypt(appt.appointmentDate) + " " +
                                            CryptClass.decrypt(appt.appointmentTime)
                            );
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
                }
            }

            //  OFFLINE FALLBACK
            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {

                new Thread(() -> {

                    AppointmentRepository repo = new AppointmentRepository(RescheduleActivity.this);

                    availableAppointments =
                            repo.getAvailableAppointmentsOffline(hospitalId);

                    List<String> timeSlots = new ArrayList<>();

                    for (AppointmentRequest appt : availableAppointments) {

                        timeSlots.add(
                                CryptClass.decrypt(appt.appointmentDate) + " " +
                                        CryptClass.decrypt(appt.appointmentTime)
                        );
                    }

                    runOnUiThread(() -> {

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                RescheduleActivity.this,
                                android.R.layout.simple_spinner_item,
                                timeSlots
                        );

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
                    });
                }).start();
            }
        });
    }


    private void cancelAndBook(AppointmentRequest newAppointment) {

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("appointmentID", oldAppointmentId);

        apiService.cancelAppointment(cancelBody).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> cancelResponse) {

                if (cancelResponse.isSuccessful()) {

                    Map<String, Object> bookBody = new HashMap<>();
                    bookBody.put("appointmentID", newAppointment.appointmentID);
                    bookBody.put("userID", userId);

                    apiService.bookAppointment(bookBody).enqueue(new Callback<Void>() {

                        @Override
                        public void onResponse(Call<Void> call, Response<Void> bookResponse) {

                            // ALSO UPDATE OFFLINE DB
                            new Thread(() -> {
                                AppointmentRepository repo =
                                        new AppointmentRepository(RescheduleActivity.this);

                                repo.cancelAppointmentOfflineByAnyId(oldAppointmentId);
                                repo.bookAppointmentOfflineByAnyId(
                                        newAppointment.appointmentID,
                                        userId
                                );
                            }).start();

                            Toast.makeText(RescheduleActivity.this,
                                    "Appointment rescheduled!",
                                    Toast.LENGTH_SHORT).show();

                            finish();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RescheduleActivity.this,
                                    "Error booking new: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            // FULL OFFLINE RESCHEDULE
            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                new Thread(() -> {

                    AppointmentRepository repo =
                            new AppointmentRepository(RescheduleActivity.this);

                    boolean canceled =
                            repo.cancelAppointmentOfflineByAnyId(oldAppointmentId);

                    boolean booked =
                            repo.bookAppointmentOfflineByAnyId(
                                    newAppointment.appointmentID,
                                    userId
                            );

                    runOnUiThread(() -> {

                        if (canceled && booked) {
                            Toast.makeText(RescheduleActivity.this,
                                    "Offline: Appointment rescheduled!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RescheduleActivity.this,
                                    "Offline reschedule failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }).start();
            }
        });
    }


}
