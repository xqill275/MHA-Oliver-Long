package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.ApiService;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Button registerBtn, decryptBtn, encryptBtn, loginBtn;
    TextView userListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerBtn = findViewById(R.id.RegisterButton);
        decryptBtn = findViewById(R.id.DecryptButton);
        encryptBtn = findViewById(R.id.EncryptButton);
        loginBtn = findViewById(R.id.LoginButton);
        userListText = findViewById(R.id.UserListText);

        // Buttons
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterPage.class))
        );

        loginBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginPage.class))
        );

        decryptBtn.setOnClickListener(v -> fetchAndDisplayUsers(false));
        encryptBtn.setOnClickListener(v -> fetchAndDisplayUsers(true));

        // Fetch encrypted users by default
        fetchAndDisplayUsers(true);
    }

    private void fetchAndDisplayUsers(boolean encrypted) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUsers().enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayUsers(response.body(), encrypted);
                } else {
                    userListText.setText("No users found or server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                userListText.setText("Network error: " + t.getMessage());
                Log.e("API", "Network error: " + t.getMessage());
            }
        });
    }

    private void displayUsers(List<UserRequest> users, boolean encrypted) {
        if (users.isEmpty()) {
            userListText.setText("No users registered yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (UserRequest user : users) {
            String name = encrypted ? user.FullName : CryptClass.decrypt(user.FullName);
            String email = encrypted ? user.Email : CryptClass.decrypt(user.Email);
            String nhs = encrypted ? user.NHSnum : CryptClass.decrypt(user.NHSnum);
            String dob = encrypted ? user.DateOfBirth : CryptClass.decrypt(user.DateOfBirth);
            String phone = encrypted ? user.PhoneNum : CryptClass.decrypt(user.PhoneNum);
            String role = encrypted ? user.Role : CryptClass.decrypt(user.Role);

            sb.append("Name: ").append(name)
                    .append("\nEmail: ").append(email)
                    .append("\nNHS: ").append(nhs)
                    .append("\nDOB: ").append(dob)
                    .append("\nPhone: ").append(phone)
                    .append("\nRole: ").append(role)
                    .append("\n---------------------\n");
        }
        userListText.setText(sb.toString());
    }
}
