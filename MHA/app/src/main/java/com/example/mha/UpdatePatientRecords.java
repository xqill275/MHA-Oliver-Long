package com.example.mha;

import com.example.mha.network.ApiService;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;
import com.example.mha.network.RecordRequest;
import com.example.mha.network.VitalsRequest; // <-- NEW

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePatientRecords extends AppCompatActivity {

    private ApiService apiService;
    private Spinner userSpinner;
    private EditText allergiesInput, medicationsInput, problemsInput;
    private EditText temperatureInput, heartRateInput, systolicInput, diastolicInput; // NEW
    private Button saveButton, saveVitalsButton; // NEW

    private List<UserRequest> users = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private int selectedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_patient_records);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        userSpinner = findViewById(R.id.userSpinner);
        allergiesInput = findViewById(R.id.allergiesInput);
        medicationsInput = findViewById(R.id.medicationsInput);
        problemsInput = findViewById(R.id.problemsInput);
        saveButton = findViewById(R.id.saveButton);

        // Vitals
        temperatureInput = findViewById(R.id.temperatureInput);
        heartRateInput = findViewById(R.id.heartRateInput);
        systolicInput = findViewById(R.id.systolicInput);
        diastolicInput = findViewById(R.id.diastolicInput);
        saveVitalsButton = findViewById(R.id.saveVitalsButton);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        fetchUsersFromApi();

        // Spinner selection listener
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = users.get(position).UID;
                fetchRecordForUser(selectedUserId);
                fetchVitalsForUser(selectedUserId); // NEW
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserId = -1;
            }
        });

        // Save Record button
        saveButton.setOnClickListener(v -> {
            if (selectedUserId == -1) {
                Toast.makeText(UpdatePatientRecords.this, "Please select a user", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                saveRecordForUser(selectedUserId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Save Vitals button
        saveVitalsButton.setOnClickListener(v -> {
            if (selectedUserId == -1) {
                Toast.makeText(UpdatePatientRecords.this, "Please select a user", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                saveVitalsForUser(selectedUserId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void fetchUsersFromApi() {
        apiService.getUsers().enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users = response.body();
                    userNames.clear();

                    for (UserRequest user : users) {
                        try {
                            String name = CryptClass.decrypt(user.FullName);
                            userNames.add(name + " (ID: " + user.UID + ")");
                        } catch (Exception e) {
                            Log.e("UpdateRecords", "Decrypt error for user " + user.UID, e);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdatePatientRecords.this,
                            android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(UpdatePatientRecords.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecordForUser(int userID) {
        apiService.getRecord(userID).enqueue(new Callback<RecordRequest>() {
            @Override
            public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecordRequest record = response.body();
                    try {
                        allergiesInput.setText(record.allergies != null ? CryptClass.decrypt(record.allergies) : "");
                        medicationsInput.setText(record.medications != null ? CryptClass.decrypt(record.medications) : "");
                        problemsInput.setText(record.problems != null ? CryptClass.decrypt(record.problems) : "");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    allergiesInput.setText("");
                    medicationsInput.setText("");
                    problemsInput.setText("");
                }
            }

            @Override
            public void onFailure(Call<RecordRequest> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Failed to load record", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🩺 NEW - Fetch vitals
    private void fetchVitalsForUser(int userID) {
        apiService.getVitals(userID).enqueue(new Callback<VitalsRequest>() {
            @Override
            public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VitalsRequest vitals = response.body();
                    temperatureInput.setText(String.valueOf(CryptClass.decrypt(String.valueOf(vitals.temperature))));
                    heartRateInput.setText(String.valueOf(CryptClass.decrypt(String.valueOf(vitals.heartRate))));
                    systolicInput.setText(String.valueOf(CryptClass.decrypt(String.valueOf(vitals.systolic))));
                    diastolicInput.setText(String.valueOf(CryptClass.decrypt(String.valueOf(vitals.diastolic))));
                } else {
                    temperatureInput.setText("");
                    heartRateInput.setText("");
                    systolicInput.setText("");
                    diastolicInput.setText("");
                }
            }

            @Override
            public void onFailure(Call<VitalsRequest> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Failed to load vitals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🩺 NEW - Save vitals
    private void saveVitalsForUser(int userID) throws Exception {
        double temperature = temperatureInput.getText().toString().isEmpty() ? 0 : Double.parseDouble(temperatureInput.getText().toString());
        int heartRate = heartRateInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(heartRateInput.getText().toString());
        int systolic = systolicInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(systolicInput.getText().toString());
        int diastolic = diastolicInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(diastolicInput.getText().toString());
        String encryptedTemperature = CryptClass.encrypt(String.valueOf(temperature));
        String encryptedHeartRate = CryptClass.encrypt(String.valueOf(heartRate));
        String encryptedSystolic = CryptClass.encrypt(String.valueOf(systolic));
        String encryptedDiastolic = CryptClass.encrypt(String.valueOf(diastolic));


        VitalsRequest vitals = new VitalsRequest(userID, encryptedTemperature, encryptedHeartRate, encryptedSystolic, encryptedDiastolic);

        apiService.updateVitals(vitals).enqueue(new Callback<VitalsRequest>() {
            @Override
            public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UpdatePatientRecords.this, "Vitals saved successfully", Toast.LENGTH_SHORT).show();
                    fetchVitalsForUser(userID);
                } else {
                    Toast.makeText(UpdatePatientRecords.this, "Failed to save vitals", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VitalsRequest> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Error saving vitals: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecordForUser(int userID) throws Exception {
        RecordRequest record = new RecordRequest(
                userID,
                CryptClass.encrypt(allergiesInput.getText().toString().trim()),
                CryptClass.encrypt(medicationsInput.getText().toString().trim()),
                CryptClass.encrypt(problemsInput.getText().toString().trim())
        );

        apiService.updateRecord(record).enqueue(new Callback<RecordRequest>() {
            @Override
            public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UpdatePatientRecords.this, "Record saved successfully", Toast.LENGTH_SHORT).show();
                    fetchRecordForUser(userID);
                } else {
                    Toast.makeText(UpdatePatientRecords.this, "Failed to save record", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecordRequest> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Error saving record: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
