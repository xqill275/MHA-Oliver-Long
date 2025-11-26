package com.example.mha;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mha.database.entities.AppointmentEntity;
import com.example.mha.database.entities.HospitalEntity;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.ApiService;
import com.example.mha.network.UserRequest;
import com.example.mha.repository.AppointmentRepository;
import com.example.mha.repository.HospitalRepository;
import com.example.mha.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPage extends AppCompatActivity {

    Spinner userSpinner, roleSpinner, hospitalSpinner;
    Button updateRoleButton, addHospitalButton, addAppointmentButton;
    EditText hospitalNameInput, hospitalCityInput, hospitalPostcodeInput;
    EditText appointmentDateInput, appointmentTimeInput;

    List<UserRequest> users = new ArrayList<>();
    List<String> userNames = new ArrayList<>();
    List<HospitalRequest> hospitals = new ArrayList<>();
    List<String> hospitalNames = new ArrayList<>();

    ApiService apiService;
    UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        // 🔹 UI References
        userSpinner = findViewById(R.id.userSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        updateRoleButton = findViewById(R.id.updateRoleButton);

        hospitalNameInput = findViewById(R.id.hospitalNameInput);
        hospitalCityInput = findViewById(R.id.hospitalCityInput);
        hospitalPostcodeInput = findViewById(R.id.hospitalPostcodeInput);
        addHospitalButton = findViewById(R.id.addHospitalButton);

        hospitalSpinner = findViewById(R.id.hospitalSpinner);
        appointmentDateInput = findViewById(R.id.appointmentDateInput);
        appointmentTimeInput = findViewById(R.id.appointmentTimeInput);
        addAppointmentButton = findViewById(R.id.addAppointmentButton);

        // ✅ Repositories
        userRepo = new UserRepository(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        HospitalRepository hospitalRepository = new HospitalRepository(this);


        // ✅ Load users OFFLINE + ONLINE
        fetchUsers();

        // ✅ Load hospitals (still online only for now)
        fetchHospitalsFromApi();

        // 🔹 Role dropdown
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Patient", "Admin", "Doctor"}
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // ✅ OFFLINE + ONLINE UPDATE ROLE
        updateRoleButton.setOnClickListener(v -> {

            int selectedUserIndex = userSpinner.getSelectedItemPosition();
            if (selectedUserIndex < 0 || selectedUserIndex >= users.size()) {
                Toast.makeText(this, "Please select a user.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRequest selectedUser = users.get(selectedUserIndex);

            String encryptedRole;
            try {
                encryptedRole = CryptClass.encrypt(
                        roleSpinner.getSelectedItem().toString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            updateUserRole(selectedUser.UID, encryptedRole);
        });

        // 🔹 Add hospital button (unchanged)
        addHospitalButton.setOnClickListener(v -> {
            String name = hospitalNameInput.getText().toString().trim();
            String city = hospitalCityInput.getText().toString().trim();
            String postcode = hospitalPostcodeInput.getText().toString().trim();

            if (name.isEmpty() || city.isEmpty() || postcode.isEmpty()) {
                Toast.makeText(this, "Please fill in all hospital details.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                addHospital(
                        CryptClass.encrypt(name),
                        CryptClass.encrypt(city),
                        CryptClass.encrypt(postcode)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 🔹 Add appointment button (unchanged)
        addAppointmentButton.setOnClickListener(v -> {

            int selectedHospitalIndex = hospitalSpinner.getSelectedItemPosition();
            if (selectedHospitalIndex < 0 || selectedHospitalIndex >= hospitals.size()) {
                Toast.makeText(this, "Please select a hospital.", Toast.LENGTH_SHORT).show();
                return;
            }

            int hospitalID = hospitals.get(selectedHospitalIndex).hospitalID;
            String date = appointmentDateInput.getText().toString().trim();
            String time = appointmentTimeInput.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill in both date and time.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                addAppointment(hospitalID, date, time);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ✅ OFFLINE + ONLINE USER FETCH
    private void fetchUsers() {

        userRepo.getUsers(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {

                if (response.body() == null) {
                    Toast.makeText(AdminPage.this, "No users found", Toast.LENGTH_SHORT).show();
                    return;
                }

                users = response.body();
                userNames.clear();

                for (UserRequest user : users) {
                    try {
                        String name = CryptClass.decrypt(user.FullName);
                        userNames.add(name + " (ID: " + user.UID + ")");
                    } catch (Exception e) {
                        Log.e("AdminPage", "Decrypt error", e);
                    }
                }

                ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                        AdminPage.this,
                        android.R.layout.simple_spinner_item,
                        userNames
                );

                userAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );

                userSpinner.setAdapter(userAdapter);
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(AdminPage.this,
                        "Error loading users: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ OFFLINE + ONLINE ROLE UPDATE
    private void updateUserRole(int uid, String encryptedRole) {

        userRepo.updateUserRole(uid, encryptedRole,
                new UserRepository.RepositoryCallback() {

                    @Override
                    public void onSuccess() {
                        runOnUiThread(() ->
                                Toast.makeText(AdminPage.this,
                                        "Role updated successfully!",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onFailure(String message) {
                        runOnUiThread(() ->
                                Toast.makeText(AdminPage.this,
                                        "Update failed: " + message,
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    // 🔹 Fetch hospitals (unchanged)
    private void fetchHospitalsFromApi() {

        apiService.getHospitals().enqueue(new Callback<List<HospitalRequest>>() {
            @Override
            public void onResponse(Call<List<HospitalRequest>> call,
                                   Response<List<HospitalRequest>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    hospitals = response.body();
                    hospitalNames.clear();

                    for (HospitalRequest hospital : hospitals) {
                        try {
                            hospitalNames.add(
                                    CryptClass.decrypt(hospital.name) +
                                            " (" +
                                            CryptClass.decrypt(hospital.city)
                                            + ")"
                            );
                        } catch (Exception ignored) {}
                    }

                    ArrayAdapter<String> hospitalAdapter =
                            new ArrayAdapter<>(AdminPage.this,
                                    android.R.layout.simple_spinner_item,
                                    hospitalNames
                            );

                    hospitalAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    hospitalSpinner.setAdapter(hospitalAdapter);

                } else {
                    Toast.makeText(AdminPage.this,
                            "Failed to load hospitals",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HospitalRequest>> call, Throwable t) {
                Toast.makeText(AdminPage.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Add hospital (unchanged)
    private void addHospital(String name, String city, String postcode) {

        HospitalRequest hospital =
                new HospitalRequest(name, city, postcode);
        HospitalRepository hospitalRepository = new HospitalRepository(this);


        apiService.addHospital(hospital).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {

                    new Thread(() -> {

                        HospitalEntity entity = new HospitalEntity();
                        entity.name = name;
                        entity.city = city;
                        entity.postcode = postcode;

                        // ✅ If your backend auto-generates hospitalID,
                        // you MUST fetch hospitals again to get the real ID
                        entity.hospitalID = 0; // temp fallback ONLY

                        hospitalRepository.insert(entity);

                    }).start();

                    Toast.makeText(AdminPage.this,
                            "Hospital added successfully!",
                            Toast.LENGTH_SHORT).show();

                    hospitalNameInput.setText("");
                    hospitalCityInput.setText("");
                    hospitalPostcodeInput.setText("");

                    fetchHospitalsFromApi(); // ✅ pulls real server IDs

                } else {
                    Toast.makeText(AdminPage.this,
                            "Failed to add hospital",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                // ✅ OFFLINE SAVE
                new Thread(() -> {

                    HospitalEntity entity = new HospitalEntity();
                    entity.name = name;
                    entity.city = city;
                    entity.postcode = postcode;

                    entity.hospitalID = -1; // ✅ OFFLINE TEMP ID

                    hospitalRepository.insert(entity);

                }).start();

                Toast.makeText(AdminPage.this,
                        "Offline: Hospital saved locally & will sync later",
                        Toast.LENGTH_LONG).show();

                hospitalNameInput.setText("");
                hospitalCityInput.setText("");
                hospitalPostcodeInput.setText("");
            }
        });
    }

    // 🔹 Add appointment (unchanged)
    private void addAppointment(int hospitalID,
                                String date,
                                String time) throws Exception {

        AppointmentRequest appointment =
                new AppointmentRequest(
                        hospitalID,
                        CryptClass.encrypt(date),
                        CryptClass.encrypt(time),
                        "available"
                );

        AppointmentRepository appointmentRepo =
                new AppointmentRepository(this);

        apiService.addAppointment(appointment)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call,
                                           Response<Void> response) {

                        if (response.isSuccessful()) {

                            new Thread(() -> {

                                AppointmentEntity entity =
                                        new AppointmentEntity();

                                entity.hospitalID = hospitalID;
                                entity.userID = null;
                                entity.appointmentDate =
                                        appointment.appointmentDate;
                                entity.appointmentTime =
                                        appointment.appointmentTime;
                                entity.status = "available";

                                entity.appointmentID = 0; // ✅ TEMP — replaced on sync

                                appointmentRepo.getAllAppointments();
                            }).start();

                            Toast.makeText(AdminPage.this,
                                    "Appointment added successfully!",
                                    Toast.LENGTH_SHORT).show();

                            appointmentDateInput.setText("");
                            appointmentTimeInput.setText("");

                        } else {
                            Toast.makeText(AdminPage.this,
                                    "Failed to add appointment",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        // ✅ OFFLINE SAVE
                        new Thread(() -> {

                            appointmentRepo.addAppointmentSlotOffline(
                                    hospitalID,
                                    appointment.appointmentDate,
                                    appointment.appointmentTime
                            );

                        }).start();

                        Toast.makeText(AdminPage.this,
                                "Offline: Appointment saved locally",
                                Toast.LENGTH_LONG).show();

                        appointmentDateInput.setText("");
                        appointmentTimeInput.setText("");
                    }
                });
    }



}
