package com.example.mha;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mha.database.entities.RecordEntity;
import com.example.mha.database.entities.VitalEntity;
import com.example.mha.network.ApiService;
import com.example.mha.network.RecordRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;
import com.example.mha.network.VitalsRequest;
import com.example.mha.repository.RecordRepository;
import com.example.mha.repository.UserRepository;
import com.example.mha.repository.VitalsRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPatientRecords extends AppCompatActivity {

    private ApiService apiService;
    private UserRepository userRepo;
    private RecordRepository recordRepo;
    private VitalsRepository vitalsRepo;

    private Spinner userSpinner;
    private TextView recordText, vitalsText;
    Button scanBarcodeBtn;

    private List<UserRequest> users = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private int selectedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_patient_records);

        userSpinner = findViewById(R.id.userSpinner);
        recordText = findViewById(R.id.recordText);
        vitalsText = findViewById(R.id.vitalsText);
        scanBarcodeBtn = findViewById(R.id.scanBarcodeBtn);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        userRepo = new UserRepository(this);
        recordRepo = new RecordRepository(this);
        vitalsRepo = new VitalsRepository(this);

        fetchUsers();

        scanBarcodeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPatientRecords.this, BarcodeScannerActivity.class);
            startActivityForResult(intent, 2001);
        });

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = users.get(position).UID;
                if (selectedUserId != -1) {
                    fetchRecordForUser(selectedUserId);
                    fetchVitalsForUser(selectedUserId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserId = -1;
            }
        });
    }


    // USERS (ONLINE + OFFLINE)


    private void fetchUsers() {

        userRepo.getUsers(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {

                if (response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(ViewPatientRecords.this, "No users available", Toast.LENGTH_SHORT).show();
                    return;
                }

                users = response.body();
                userNames.clear();

                for (UserRequest user : users) {
                    try {
                        userNames.add(CryptClass.decrypt(user.FullName) + " (ID: " + user.UID + ")");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                userSpinner.setAdapter(new ArrayAdapter<>(
                        ViewPatientRecords.this,
                        android.R.layout.simple_spinner_item,
                        userNames
                ));
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(ViewPatientRecords.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // RECORD (ONLINE + OFFLINE)


    private void fetchRecordForUser(int userID) {

        apiService.getRecord(userID).enqueue(new Callback<RecordRequest>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {

                if (response.isSuccessful() && response.body() != null) {

                    RecordRequest r = response.body();

                    recordRepo.upsertRecord(
                            userID,
                            r.allergies,
                            r.medications,
                            r.problems
                    );

                    try {
                        recordText.setText(
                                "Allergies: " + CryptClass.decrypt(r.allergies) + "\n" +
                                        "Medications: " + CryptClass.decrypt(r.medications) + "\n" +
                                        "Problems: " + CryptClass.decrypt(r.problems)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(Call<RecordRequest> call, Throwable t) {

                // OFFLINE FALLBACK
                RecordEntity local = recordRepo.getByUser(userID);
                if (local != null) {
                    try {
                        recordText.setText(
                                "Allergies: " + CryptClass.decrypt(local.allergies) + "\n" +
                                        "Medications: " + CryptClass.decrypt(local.medications) + "\n" +
                                        "Problems: " + CryptClass.decrypt(local.problems)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    recordText.setText("No record found (offline)");
                }
            }
        });
    }


    //  VITALS (ONLINE + OFFLINE)


    private void fetchVitalsForUser(int userID) {

        apiService.getVitals(userID).enqueue(new Callback<VitalsRequest>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {

                if (response.isSuccessful() && response.body() != null) {

                    VitalsRequest v = response.body();

                    vitalsRepo.upsertVitals(
                            userID,
                            v.temperature,
                            v.heartRate,
                            v.systolic,
                            v.diastolic
                    );

                    try {
                        vitalsText.setText(
                                "Temperature: " + CryptClass.decrypt(v.temperature) + "°C\n" +
                                        "Heart Rate: " + CryptClass.decrypt(v.heartRate) + " bpm\n" +
                                        "Systolic: " + CryptClass.decrypt(v.systolic) + " mmHg\n" +
                                        "Diastolic: " + CryptClass.decrypt(v.diastolic) + " mmHg"
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(Call<VitalsRequest> call, Throwable t) {

                // OFFLINE FALLBACK
                VitalEntity local = vitalsRepo.getLatestVitalsForUser(userID);
                if (local != null) {
                    try {
                        vitalsText.setText(
                                "Temperature: " + CryptClass.decrypt(local.temperature) + "°C\n" +
                                        "Heart Rate: " + CryptClass.decrypt(local.heartRate) + " bpm\n" +
                                        "Systolic: " + CryptClass.decrypt(local.systolic) + " mmHg\n" +
                                        "Diastolic: " + CryptClass.decrypt(local.diastolic) + " mmHg"
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    vitalsText.setText("No vitals found (offline)");
                }
            }
        });
    }


    // BARCODE RESULT


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK) {
            int scannedUserId = data.getIntExtra("userID", -1);

            if (scannedUserId != -1) {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).UID == scannedUserId) {

                        userSpinner.setSelection(i);

                        fetchRecordForUser(scannedUserId);
                        fetchVitalsForUser(scannedUserId);
                        break;
                    }
                }
            }
        }
    }
}
