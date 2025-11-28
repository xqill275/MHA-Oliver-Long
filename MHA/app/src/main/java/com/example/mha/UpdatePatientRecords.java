package com.example.mha;

import com.example.mha.network.ApiService;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;
import com.example.mha.network.RecordRequest;
import com.example.mha.network.VitalsRequest;

import com.example.mha.repository.RecordRepository;
import com.example.mha.repository.UserRepository;
import com.example.mha.repository.VitalsRepository;
import com.example.mha.database.entities.RecordEntity;
import com.example.mha.database.entities.VitalEntity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePatientRecords extends AppCompatActivity {

    private ApiService apiService;
    private VitalsRepository vitalsRepo;
    private RecordRepository recordRepo;
    private UserRepository userRepo;


    private Spinner userSpinner;
    private EditText allergiesInput, medicationsInput, problemsInput;
    private EditText temperatureInput, heartRateInput, systolicInput, diastolicInput;
    private Button saveButton, saveVitalsButton, updateRecordsBack;

    private List<UserRequest> users = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private int selectedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_patient_records);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        userRepo = new UserRepository(this);
        vitalsRepo = new VitalsRepository(this);
        recordRepo = new RecordRepository(this);

        userSpinner = findViewById(R.id.userSpinner);
        allergiesInput = findViewById(R.id.allergiesInput);
        medicationsInput = findViewById(R.id.medicationsInput);
        problemsInput = findViewById(R.id.problemsInput);
        temperatureInput = findViewById(R.id.temperatureInput);
        heartRateInput = findViewById(R.id.heartRateInput);
        systolicInput = findViewById(R.id.systolicInput);
        diastolicInput = findViewById(R.id.diastolicInput);
        saveButton = findViewById(R.id.saveButton);
        saveVitalsButton = findViewById(R.id.saveVitalsButton);

        updateRecordsBack = findViewById(R.id.updateRecordsBack);


        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");

        updateRecordsBack.setOnClickListener(v -> {
            Intent Backintent = new Intent(UpdatePatientRecords.this, MedicalRecords.class);
            Backintent.putExtra("UserId", userId);
            Backintent.putExtra("UserRole", userRole);
            startActivity(Backintent);
        });

        fetchUsersFromApi();

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = users.get(position).UID;
                fetchRecordForUser(selectedUserId);
                fetchVitalsForUser(selectedUserId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserId = -1;
            }
        });

        saveButton.setOnClickListener(v -> saveRecordForUser(selectedUserId));
        saveVitalsButton.setOnClickListener(v -> saveVitalsForUser(selectedUserId));
    }


    // RECORDS (ONLINE + OFFLINE)


    private void fetchRecordForUser(int userID) {

        apiService.getRecord(userID).enqueue(new Callback<RecordRequest>() {
            @Override
            public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {

                if (response.isSuccessful() && response.body() != null) {

                    RecordRequest record = response.body();

                    try {
                        recordRepo.upsertRecord(
                                userID,
                                record.allergies,
                                record.medications,
                                record.problems
                        );

                        allergiesInput.setText(CryptClass.decrypt(record.allergies));
                        medicationsInput.setText(CryptClass.decrypt(record.medications));
                        problemsInput.setText(CryptClass.decrypt(record.problems));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RecordRequest> call, Throwable t) {
                // OFFLINE FALLBACK
                RecordEntity local = recordRepo.getByUser(userID);
                if (local != null) {
                    try {
                        allergiesInput.setText(CryptClass.decrypt(local.allergies));
                        medicationsInput.setText(CryptClass.decrypt(local.medications));
                        problemsInput.setText(CryptClass.decrypt(local.problems));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveRecordForUser(int userID) {

        try {
            String allergies = CryptClass.encrypt(allergiesInput.getText().toString().trim());
            String medications = CryptClass.encrypt(medicationsInput.getText().toString().trim());
            String problems = CryptClass.encrypt(problemsInput.getText().toString().trim());

            // SAVE LOCALLY FIRST
            recordRepo.upsertRecord(userID, allergies, medications, problems);

            RecordRequest record = new RecordRequest(userID, allergies, medications, problems);

            apiService.updateRecord(record).enqueue(new Callback<RecordRequest>() {
                @Override
                public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {
                    Toast.makeText(UpdatePatientRecords.this, "Record synced", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<RecordRequest> call, Throwable t) {
                    Toast.makeText(UpdatePatientRecords.this, "Saved offline", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // VITALS (ONLINE + OFFLINE)


    private void fetchVitalsForUser(int userID) {

        apiService.getVitals(userID).enqueue(new Callback<VitalsRequest>() {
            @Override
            public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {

                if (response.isSuccessful() && response.body() != null) {

                    VitalsRequest v = response.body();

                    vitalsRepo.upsertVitals(userID,
                            v.temperature,
                            v.heartRate,
                            v.systolic,
                            v.diastolic
                    );

                    try {
                        temperatureInput.setText(CryptClass.decrypt(v.temperature));
                        heartRateInput.setText(CryptClass.decrypt(v.heartRate));
                        systolicInput.setText(CryptClass.decrypt(v.systolic));
                        diastolicInput.setText(CryptClass.decrypt(v.diastolic));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<VitalsRequest> call, Throwable t) {

                // OFFLINE FALLBACK
                VitalEntity local = vitalsRepo.getLatestVitalsForUser(userID);
                if (local != null) {
                    try {
                        temperatureInput.setText(CryptClass.decrypt(local.temperature));
                        heartRateInput.setText(CryptClass.decrypt(local.heartRate));
                        systolicInput.setText(CryptClass.decrypt(local.systolic));
                        diastolicInput.setText(CryptClass.decrypt(local.diastolic));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveVitalsForUser(int userID) {

        try {
            String temp = CryptClass.encrypt(temperatureInput.getText().toString());
            String hr = CryptClass.encrypt(heartRateInput.getText().toString());
            String sys = CryptClass.encrypt(systolicInput.getText().toString());
            String dia = CryptClass.encrypt(diastolicInput.getText().toString());

            // SAVE LOCALLY FIRST
            vitalsRepo.upsertVitals(userID, temp, hr, sys, dia);

            VitalsRequest v = new VitalsRequest(userID, temp, hr, sys, dia);

            apiService.updateVitals(v).enqueue(new Callback<VitalsRequest>() {
                @Override
                public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {
                    Toast.makeText(UpdatePatientRecords.this, "Vitals synced", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<VitalsRequest> call, Throwable t) {
                    Toast.makeText(UpdatePatientRecords.this, "Vitals saved offline", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





// USERS (ONLINE + OFFLINE)


    private void fetchUsersFromApi() {

        userRepo.getUsers(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {

                if (response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(UpdatePatientRecords.this, "No users available", Toast.LENGTH_SHORT).show();
                    return;
                }

                users = response.body();
                userNames.clear();

                for (UserRequest user : users) {
                    try {
                        userNames.add(CryptClass.decrypt(user.FullName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                userSpinner.setAdapter(new ArrayAdapter<>(
                        UpdatePatientRecords.this,
                        android.R.layout.simple_spinner_item,
                        userNames
                ));

                Toast.makeText(UpdatePatientRecords.this, "Users loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(UpdatePatientRecords.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
