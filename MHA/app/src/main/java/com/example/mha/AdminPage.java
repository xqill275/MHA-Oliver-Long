package com.example.mha;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mha.network.ApiService;
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

    Spinner userSpinner, roleSpinner;
    Button updateRoleButton;
    List<UserRequest> users = new ArrayList<>();
    List<String> userNames = new ArrayList<>();
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        userSpinner = findViewById(R.id.userSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        updateRoleButton = findViewById(R.id.updateRoleButton);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Fetch users from API
        fetchUsersFromApi();

        // Role dropdown
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Patient", "Admin"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Update role
        updateRoleButton.setOnClickListener(v -> {
            int selectedUserIndex = userSpinner.getSelectedItemPosition();
            if (selectedUserIndex < 0 || selectedUserIndex >= users.size()) {
                Toast.makeText(this, "Please select a user.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRequest selectedUser = users.get(selectedUserIndex);
            String selectedRole = null;
            try {
                selectedRole = CryptClass.encrypt(roleSpinner.getSelectedItem().toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            updateUserRole(selectedUser.UID, selectedRole);
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
                        String name = CryptClass.decrypt(user.FullName);
                        userNames.add(name + " (ID: " + user.UID + ")");
                    }

                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(AdminPage.this, android.R.layout.simple_spinner_item, userNames);
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
}
