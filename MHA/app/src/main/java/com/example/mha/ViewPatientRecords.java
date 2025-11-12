package com.example.mha;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.ApiService;
import com.example.mha.network.RecordRequest;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;
import com.example.mha.network.VitalsRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPatientRecords extends AppCompatActivity {

    private ApiService apiService;
    private Spinner userSpinner;
    private TextView recordText, vitalsText;

    private List<UserRequest> users = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private int selectedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_patient_records);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userSpinner = findViewById(R.id.userSpinner);
        recordText = findViewById(R.id.recordText);
        vitalsText = findViewById(R.id.vitalsText);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        fetchUsersFromApi();

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
                            Log.e("ViewRecords", "Decrypt error for user " + user.UID, e);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewPatientRecords.this,
                            android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(ViewPatientRecords.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(ViewPatientRecords.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecordForUser(int userID) {
        apiService.getRecord(userID).enqueue(new Callback<RecordRequest>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<RecordRequest> call, Response<RecordRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecordRequest record = response.body();
                    recordText.setText(
                            "Allergies: " + record.allergies + "\n" +
                                    "Medications: " + record.medications + "\n" +
                                    "Problems: " + record.problems + "\n");
                } else {
                    recordText.setText("No medical record found for this user.");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(Call<RecordRequest> call, Throwable t) {
                recordText.setText("Failed to load record: " + t.getMessage());
            }
        });
    }

    private void fetchVitalsForUser(int userID) {
        apiService.getVitals(userID).enqueue(new Callback<VitalsRequest>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<VitalsRequest> call, Response<VitalsRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VitalsRequest vitals = response.body();
                    vitalsText.setText(
                            "Temperature: " + vitals.temperature + "°C\n" +
                                    "Heart Rate: " + vitals.heartRate + " bpm\n" +
                                    "Systolic: " + vitals.systolic + " mmHg\n" +
                                    "Diastolic: " + vitals.diastolic + " mmHg\n");
                } else {
                    vitalsText.setText("No vitals found for this user.");
                }
            }

            @Override
            public void onFailure(Call<VitalsRequest> call, Throwable t) {
                vitalsText.setText("Failed to load vitals: " + t.getMessage());
            }
        });
    }
}
