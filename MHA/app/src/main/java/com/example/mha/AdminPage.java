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

        userRepo = new UserRepository(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        fetchUsers();
        fetchHospitalsFromApi();

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Patient", "Admin", "Doctor"}
        );
        roleSpinner.setAdapter(roleAdapter);

        updateRoleButton.setOnClickListener(v -> {
            int index = userSpinner.getSelectedItemPosition();
            if (index < 0 || index >= users.size()) return;

            try {
                updateUserRole(
                        users.get(index).UID,
                        CryptClass.encrypt(roleSpinner.getSelectedItem().toString())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addHospitalButton.setOnClickListener(v -> {
            try {
                addHospital(
                        CryptClass.encrypt(hospitalNameInput.getText().toString()),
                        CryptClass.encrypt(hospitalCityInput.getText().toString()),
                        CryptClass.encrypt(hospitalPostcodeInput.getText().toString())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addAppointmentButton.setOnClickListener(v -> {
            int index = hospitalSpinner.getSelectedItemPosition();
            if (index < 0 || index >= hospitals.size()) return;

            try {
                addAppointment(
                        hospitals.get(index).hospitalID,
                        appointmentDateInput.getText().toString(),
                        appointmentTimeInput.getText().toString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // USERS
    private void fetchUsers() {

        userRepo.getUsers(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {

                if (response.body() == null) return;

                users = response.body();
                userNames.clear();

                for (UserRequest user : users) {
                    try {
                        userNames.add(CryptClass.decrypt(user.FullName) + " (ID: " + user.UID + ")");
                    } catch (Exception e) {
                        Log.e("Admin", "Decrypt Error", e);
                    }
                }

                userSpinner.setAdapter(new ArrayAdapter<>(
                        AdminPage.this,
                        android.R.layout.simple_spinner_item,
                        userNames
                ));
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(AdminPage.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserRole(int uid, String encryptedRole) {

        userRepo.updateUserRole(uid, encryptedRole,
                new UserRepository.RepositoryCallback() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminPage.this, "Role Updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(AdminPage.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // HOSPITALS (ONLINE + OFFLINE)
    private void fetchHospitalsFromApi() {

        apiService.getHospitals().enqueue(new Callback<List<HospitalRequest>>() {

            @Override
            public void onResponse(Call<List<HospitalRequest>> call,
                                   Response<List<HospitalRequest>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    fetchHospitalsFromRoomFallback();
                    return;
                }

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

                hospitalSpinner.setAdapter(new ArrayAdapter<>(
                        AdminPage.this,
                        android.R.layout.simple_spinner_item,
                        hospitalNames
                ));
            }

            @Override
            public void onFailure(Call<List<HospitalRequest>> call, Throwable t) {
                fetchHospitalsFromRoomFallback();
            }
        });
    }

    private void fetchHospitalsFromRoomFallback() {

        HospitalRepository hospitalRepository = new HospitalRepository(this);

        new Thread(() -> {

            List<HospitalEntity> localHospitals = hospitalRepository.getAll();
            if (localHospitals == null || localHospitals.isEmpty()) return;

            hospitals.clear();
            hospitalNames.clear();

            for (HospitalEntity h : localHospitals) {

                HospitalRequest fakeApiObj =
                        new HospitalRequest(h.name, h.city, h.postcode);

                fakeApiObj.hospitalID = h.hospitalID;
                hospitals.add(fakeApiObj);

                try {
                    hospitalNames.add(
                            CryptClass.decrypt(h.name) +
                                    " (" +
                                    CryptClass.decrypt(h.city)
                                    + ")"
                    );
                } catch (Exception ignored) {}
            }

            runOnUiThread(() ->
                    hospitalSpinner.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            hospitalNames
                    ))
            );

        }).start();
    }

    // ADD HOSPITAL (SERVER ID PRESERVED)
    private void addHospital(String name, String city, String postcode) {

        HospitalRepository repo = new HospitalRepository(this);

        apiService.addHospital(new HospitalRequest(name, city, postcode))
                .enqueue(new Callback<Void>() {   // FIXED GENERIC TYPE

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        new Thread(() -> {

                            HospitalEntity e = new HospitalEntity();
                            e.name = name;
                            e.city = city;
                            e.postcode = postcode;

                            // Let ROOM auto-generate ID safely
                            repo.insert(e);

                        }).start();

                        // Pull real server IDs back in
                        fetchHospitalsFromApi();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        // OFFLINE SAVE
                        new Thread(() -> {

                            HospitalEntity e = new HospitalEntity();
                            e.name = name;
                            e.city = city;
                            e.postcode = postcode;

                            //  Offline row (Room still auto-generates)
                            repo.insert(e);

                        }).start();

                        Toast.makeText(
                                AdminPage.this,
                                "Offline: Hospital saved locally",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    // APPOINTMENTS (ONLINE + OFFLINE)
    private void addAppointment(int hospitalID, String date, String time) throws Exception {

        AppointmentRepository repo = new AppointmentRepository(this);

        AppointmentRequest appointment =
                new AppointmentRequest(
                        hospitalID,
                        CryptClass.encrypt(date),
                        CryptClass.encrypt(time),
                        "available"
                );

        apiService.addAppointment(appointment)
                .enqueue(new Callback<Void>() {   //  FIX: MUST BE Void

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        new Thread(() -> {

                            AppointmentEntity entity = new AppointmentEntity();
                            entity.hospitalID = hospitalID;
                            entity.userID = null;
                            entity.appointmentDate = appointment.appointmentDate;
                            entity.appointmentTime = appointment.appointmentTime;
                            entity.status = "available";

                            //  Let ROOM auto-generate local ID
                            repo.insert(entity);

                        }).start();

                        runOnUiThread(() -> {
                            Toast.makeText(AdminPage.this,
                                    "Appointment added (online)",
                                    Toast.LENGTH_SHORT).show();

                            appointmentDateInput.setText("");
                            appointmentTimeInput.setText("");
                        });
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        new Thread(() -> {

                            // OFFLINE SAVE
                            repo.addAppointmentSlotOffline(
                                    hospitalID,
                                    appointment.appointmentDate,
                                    appointment.appointmentTime
                            );

                        }).start();

                        runOnUiThread(() -> {
                            Toast.makeText(AdminPage.this,
                                    "Offline: Appointment saved locally",
                                    Toast.LENGTH_SHORT).show();

                            appointmentDateInput.setText("");
                            appointmentTimeInput.setText("");
                        });
                    }
                });
    }
}
