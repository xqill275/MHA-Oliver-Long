package com.example.mha;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mha.network.ApiService;
import com.example.mha.network.AppointmentRequest;
import com.example.mha.network.HospitalRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 🔹 Load data from API
        fetchUsersFromApi();
        fetchHospitalsFromApi();

        // 🔹 Role dropdown
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Patient", "Admin", "Doctor"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // 🔹 Update role button
        updateRoleButton.setOnClickListener(v -> {
            int selectedUserIndex = userSpinner.getSelectedItemPosition();
            if (selectedUserIndex < 0 || selectedUserIndex >= users.size()) {
                Toast.makeText(this, "Please select a user.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRequest selectedUser = users.get(selectedUserIndex);
            String selectedRole;
            try {
                selectedRole = CryptClass.encrypt(roleSpinner.getSelectedItem().toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            updateUserRole(selectedUser.UID, selectedRole);
        });

        // 🔹 Add hospital button
        addHospitalButton.setOnClickListener(v -> {
            String name = hospitalNameInput.getText().toString().trim();
            String city = hospitalCityInput.getText().toString().trim();
            String postcode = hospitalPostcodeInput.getText().toString().trim();

            if (name.isEmpty() || city.isEmpty() || postcode.isEmpty()) {
                Toast.makeText(this, "Please fill in all hospital details.", Toast.LENGTH_SHORT).show();
                return;
            }

            addHospital(name, city, postcode);
        });

        // 🔹 Add appointment button
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

            addAppointment(hospitalID, date, time);
        });
    }

    // 🔹 Fetch users
    private void fetchUsersFromApi() {
        apiService.getUsers().enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users = response.body();
                    userNames.clear();

                    for (UserRequest user : users) {
                        Log.d("AdminPage", "User received: UID=" + user.UID + ", FullName=" + user.FullName);
                        try {
                            String name = CryptClass.decrypt(user.FullName);
                            userNames.add(name + " (ID: " + user.UID + ")");
                        } catch (Exception e) {
                            Log.e("AdminPage", "Decrypt error for user " + user.UID, e);
                        }
                    }

                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(AdminPage.this,
                            android.R.layout.simple_spinner_item, userNames);
                    userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(userAdapter);
                } else {
                    Toast.makeText(AdminPage.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(AdminPage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminPage", "Error: " + t.getMessage());
            }
        });
    }

    // 🔹 Fetch hospitals
    private void fetchHospitalsFromApi() {
        apiService.getHospitals().enqueue(new Callback<List<HospitalRequest>>() {
            @Override
            public void onResponse(Call<List<HospitalRequest>> call, Response<List<HospitalRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hospitals = response.body();
                    hospitalNames.clear();

                    for (HospitalRequest hospital : hospitals) {
                        hospitalNames.add(hospital.name + " (" + hospital.city + ")");
                    }

                    ArrayAdapter<String> hospitalAdapter = new ArrayAdapter<>(
                            AdminPage.this,
                            android.R.layout.simple_spinner_item,
                            hospitalNames
                    );
                    hospitalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    hospitalSpinner.setAdapter(hospitalAdapter);
                } else {
                    Toast.makeText(AdminPage.this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HospitalRequest>> call, Throwable t) {
                Toast.makeText(AdminPage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminPage", "Hospital load error: " + t.getMessage());
            }
        });
    }

    // 🔹 Update role API call
    private void updateUserRole(int uid, String newRole) {
        Map<String, Object> body = new HashMap<>();
        body.put("uid", uid);
        body.put("role", newRole);

        apiService.updateUserRole(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPage.this, "Role updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminPage.this, "Failed to update role (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminPage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Add hospital API call
    private void addHospital(String name, String city, String postcode) {
        HospitalRequest hospital = new HospitalRequest(name, city, postcode);

        apiService.addHospital(hospital).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPage.this, "Hospital added successfully!", Toast.LENGTH_SHORT).show();
                    hospitalNameInput.setText("");
                    hospitalCityInput.setText("");
                    hospitalPostcodeInput.setText("");
                    fetchHospitalsFromApi(); // Refresh list
                } else {
                    Toast.makeText(AdminPage.this, "Failed to add hospital (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminPage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminPage", "Add hospital error: " + t.getMessage());
            }
        });
    }

    // 🔹 Add appointment API call
    private void addAppointment(int hospitalID, String date, String time) {
        AppointmentRequest appointment = new AppointmentRequest(hospitalID, date, time, "available");

        apiService.addAppointment(appointment).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPage.this, "Appointment added successfully!", Toast.LENGTH_SHORT).show();
                    appointmentDateInput.setText("");
                    appointmentTimeInput.setText("");
                } else {
                    Toast.makeText(AdminPage.this, "Failed to add appointment (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminPage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminPage", "Add appointment error: " + t.getMessage());
            }
        });
    }
}
