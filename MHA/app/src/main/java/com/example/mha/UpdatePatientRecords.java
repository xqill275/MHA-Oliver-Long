package com.example.mha;

import com.example.mha.network.ApiService;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;
import com.example.mha.network.RecordRequest;

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
    private Button saveButton;

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

        apiService = RetrofitClient.getClient().create(ApiService.class);

        fetchUsersFromApi();

        // Spinner selection listener
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = users.get(position).UID;
                fetchRecordForUser(selectedUserId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUserId = -1;
            }
        });

        // Save button listener
        saveButton.setOnClickListener(v -> {
            if (selectedUserId == -1) {
                Toast.makeText(UpdatePatientRecords.this, "Please select a user", Toast.LENGTH_SHORT).show();
                return;
            }
            saveRecordForUser(selectedUserId);
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
                    allergiesInput.setText(record.allergies != null ? record.allergies : "");
                    medicationsInput.setText(record.medications != null ? record.medications : "");
                    problemsInput.setText(record.problems != null ? record.problems : "");
                } else {
                    // No record found — clear fields
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

    private void saveRecordForUser(int userID) {
        RecordRequest record = new RecordRequest(
                userID,
                allergiesInput.getText().toString().trim(),
                medicationsInput.getText().toString().trim(),
                problemsInput.getText().toString().trim()
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
