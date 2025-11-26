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

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.repository.AppointmentRepository;
import com.example.mha.repository.HospitalRepository;

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
    private HospitalRepository hospitalRepo;
    private AppointmentRepository appointmentRepo;

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

        userId = getIntent().getIntExtra("UserId", -1);

        spinnerHospital = findViewById(R.id.spinnerHospital);
        spinnerTime = findViewById(R.id.spinnerTime);
        btnBook = findViewById(R.id.btnBook);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        hospitalRepo = new HospitalRepository(this);
        appointmentRepo = new AppointmentRepository(this);

        loadHospitals();

        spinnerHospital.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedHospitalId = hospitalList.get(position).hospitalID;
                loadAvailableAppointments(selectedHospitalId);
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

    // HOSPITAL LOADING (ONLINE → OFFLINE)
    private void loadHospitals() {
        apiService.getHospitals().enqueue(new Callback<List<HospitalRequest>>() {
            @Override
            public void onResponse(Call<List<HospitalRequest>> call, Response<List<HospitalRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    hospitalList = response.body();
                    List<String> names = new ArrayList<>();

                    for (HospitalRequest hospital : hospitalList) {
                        try {
                            names.add(CryptClass.decrypt(hospital.name) + " (" +
                                    CryptClass.decrypt(hospital.city) + ")");
                        } catch (Exception e) {
                            names.add("Unknown");
                        }
                    }

                    spinnerHospital.setAdapter(new ArrayAdapter<>(
                            BookActivity.this,
                            android.R.layout.simple_spinner_item,
                            names
                    ));
                } else {
                    // fallback to offline
                    loadHospitalsOffline();
                }
            }

            @Override
            public void onFailure(Call<List<HospitalRequest>> call, Throwable t) {
                Log.e("BookActivity", "Hospitals load error: " + Objects.requireNonNull(t.getMessage()));
                loadHospitalsOffline();
            }
        });
    }

    private void loadHospitalsOffline() {
        new Thread(() -> {
            hospitalList = hospitalRepo.getAllHospitalsOffline();
            List<String> names = new ArrayList<>();
            for (HospitalRequest hospital : hospitalList) {
                try {
                    names.add(CryptClass.decrypt(hospital.name));
                } catch (Exception e) {
                    names.add("Unknown");
                }
            }
            runOnUiThread(() -> spinnerHospital.setAdapter(
                    new ArrayAdapter<>(BookActivity.this,
                            android.R.layout.simple_spinner_item,
                            names)));
        }).start();
    }

    // APPOINTMENT LOADING (ONLINE → OFFLINE)
    private void loadAvailableAppointments(int hospitalId) {
        apiService.getAppointments().enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call, Response<List<AppointmentRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    appointmentList.clear();
                    List<String> slots = new ArrayList<>();

                    for (AppointmentRequest appt : response.body()) {
                        if (appt.hospitalID == hospitalId &&
                                "available".equalsIgnoreCase(appt.status)) {

                            appointmentList.add(appt);
                            try {
                                slots.add(CryptClass.decrypt(appt.appointmentDate) + " " +
                                        CryptClass.decrypt(appt.appointmentTime));
                            } catch (Exception e) {
                                slots.add("slot");
                            }
                        }
                    }

                    spinnerTime.setAdapter(new ArrayAdapter<>(BookActivity.this,
                            android.R.layout.simple_spinner_item,
                            slots));

                    spinnerTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedAppointment = appointmentList.get(position);
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                            selectedAppointment = null;
                        }
                    });
                } else {
                    // fallback to offline
                    loadAvailableAppointmentsOffline(hospitalId);
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {
                Log.e("BookActivity", "Appointments load error: " + Objects.requireNonNull(t.getMessage()));
                loadAvailableAppointmentsOffline(hospitalId);
            }
        });
    }

    private void loadAvailableAppointmentsOffline(int hospitalId) {
        new Thread(() -> {
            appointmentList = appointmentRepo.getAvailableAppointmentsOffline(hospitalId);

            List<String> slots = new ArrayList<>();
            for (AppointmentRequest appt : appointmentList) {
                try {
                    slots.add(CryptClass.decrypt(appt.appointmentDate) + " " +
                            CryptClass.decrypt(appt.appointmentTime));
                } catch (Exception e) {
                    slots.add("slot");
                }
            }

            runOnUiThread(() -> {
                spinnerTime.setAdapter(new ArrayAdapter<>(BookActivity.this,
                        android.R.layout.simple_spinner_item,
                        slots));

                spinnerTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        selectedAppointment = appointmentList.get(position);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        selectedAppointment = null;
                    }
                });
            });
        }).start();
    }

    // DOUBLE BOOKING CHECK
    private void attemptBooking(int userId, AppointmentRequest selectedAppointment) {

        // ONLINE CHECK
        apiService.getAppointmentsByUser(userId).enqueue(new Callback<List<AppointmentRequest>>() {
            @Override
            public void onResponse(Call<List<AppointmentRequest>> call,
                                   Response<List<AppointmentRequest>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    boolean hasConflict = false;
                    String newDate = selectedAppointment.appointmentDate;

                    for (AppointmentRequest appt : response.body()) {
                        if (appt.appointmentDate.equals(newDate)) {
                            hasConflict = true;
                            break;
                        }
                    }

                    if (hasConflict) {
                        Toast.makeText(BookActivity.this,
                                "You already have an appointment that day.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        bookAppointment(selectedAppointment, userId);
                    }
                } else {
                    // server response odd; fallback to offline check
                    offlineConflictAndBook(userId, selectedAppointment);
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentRequest>> call, Throwable t) {
                offlineConflictAndBook(userId, selectedAppointment);
            }
        });
    }

    private void offlineConflictAndBook(int userId, AppointmentRequest selectedAppointment) {
        new Thread(() -> {
            boolean conflict =
                    appointmentRepo.hasAppointmentForDate(userId,
                            selectedAppointment.appointmentDate);

            runOnUiThread(() -> {
                if (conflict) {
                    Toast.makeText(BookActivity.this,
                            "Offline conflict detected!",
                            Toast.LENGTH_LONG).show();
                } else {
                    bookAppointment(selectedAppointment, userId);
                }
            });
        }).start();
    }

    // FINAL BOOKING (ONLINE → OFFLINE)
    private void bookAppointment(AppointmentRequest appointment, int userId) {

        // Prepare body for API (server appointmentID expected)
        Map<String, Object> body = new HashMap<>();
        body.put("appointmentID", appointment.appointmentID);
        body.put("userID", userId);

        apiService.bookAppointment(body).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                // update local DB (resolve appointment to local row inside repo)
                new Thread(() -> {
                    appointmentRepo.bookAppointmentOffline(appointment.appointmentID, userId);
                    // refresh slots after change
                    runOnUiThread(() -> {
                        Toast.makeText(BookActivity.this,
                                "Appointment booked successfully!",
                                Toast.LENGTH_SHORT).show();
                        // refresh available slots so booked slot no longer selectable
                        loadAvailableAppointments(selectedHospitalId);
                    });
                }).start();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                // OFFLINE: use localId if present
                final int idToUse = (appointment.localId > 0) ? appointment.localId : appointment.appointmentID;

                new Thread(() -> {
                    boolean ok = appointmentRepo.bookAppointmentOffline(idToUse, userId);

                    runOnUiThread(() -> {
                        if (ok) {
                            Toast.makeText(BookActivity.this,
                                    "Offline: Appointment saved locally",
                                    Toast.LENGTH_SHORT).show();
                            loadAvailableAppointments(selectedHospitalId);
                        } else {
                            Toast.makeText(BookActivity.this,
                                    "Offline booking failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
    }
}
