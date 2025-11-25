package com.example.mha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.ApiService;
import com.example.mha.network.RetrofitClient;
import com.example.mha.network.UserRequest;

import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPage extends AppCompatActivity {

    Button BackBtn, LoginBtn, fingerprintBtn;
    TextView LoginNhs, LoginEmail, LoginDOB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BackBtn = findViewById(R.id.Back_Button_login);
        LoginBtn = findViewById(R.id.LoginActivityButton);
        fingerprintBtn = findViewById(R.id.fingerprintBtn);
        LoginNhs = findViewById(R.id.LoginNHSNUM);
        LoginEmail = findViewById(R.id.LoginEmail);
        LoginDOB = findViewById(R.id.LoginDOB);

        BackBtn.setOnClickListener(v -> startActivity(new Intent(LoginPage.this, MainActivity.class)));

        LoginBtn.setOnClickListener(v -> {
            try {
                login();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        fingerprintBtn.setOnClickListener(v -> authenticateFingerprint());
    }

    // --------------------------------------------------
    // NORMAL LOGIN (your original code)
    // --------------------------------------------------
    private void login() throws Exception {
        String nhsText = LoginNhs.getText().toString().trim();
        String emailText = LoginEmail.getText().toString().trim();
        String dobText = LoginDOB.getText().toString().trim();

        if (nhsText.isEmpty() || emailText.isEmpty() || dobText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        String emailHash = HashClass.sha256(emailText);
        String nhsHash = HashClass.sha256(nhsText);
        String dobHash = HashClass.sha256(dobText);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<UserRequest>> call = apiService.getUsers();

        call.enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean found = false;

                    for (UserRequest user : response.body()) {
                        if (user.EmailHash.equals(emailHash) &&
                                user.NHSHash.equals(nhsHash) &&
                                user.DOBHash.equals(dobHash)) {

                            found = true;

                            int userId = user.UID;
                            String decryptedName = CryptClass.decrypt(user.FullName);
                            String decryptedRole = CryptClass.decrypt(user.Role);

                            Toast.makeText(LoginPage.this,
                                    "Welcome " + decryptedName + " (" + decryptedRole + ")",
                                    Toast.LENGTH_LONG).show();

                            Log.d("Login", "Login successful for " + decryptedName);
                            Log.d("Login", "Login UID " + userId);
                            Log.d("Login", "Login role " + decryptedRole);

                            // SAVE USER INFO FOR FINGERPRINT LOGIN
                            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putInt("UserId", userId)
                                    .putString("UserRole", decryptedRole)
                                    .apply();

                            Intent intent = new Intent(LoginPage.this, MainMenu.class);
                            intent.putExtra("UserId", userId);
                            intent.putExtra("UserRole", decryptedRole);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }

                    if (!found) {
                        Toast.makeText(LoginPage.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                        Log.d("Login", "Login failed");
                    }

                } else {
                    Toast.makeText(LoginPage.this,
                            "Server error: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(LoginPage.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("Login", "Network error: " + t.getMessage());
            }
        });
    }

    // --------------------------------------------------
    // FINGERPRINT LOGIN
    // --------------------------------------------------
    private void authenticateFingerprint() {

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this,
                    "Fingerprint not available or not set up",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                LoginPage.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        int savedUserId = prefs.getInt("UserId", -1);
                        String savedRole = prefs.getString("UserRole", null);

                        if (savedUserId == -1 || savedRole == null) {
                            Toast.makeText(LoginPage.this,
                                    "Please log in normally once before using fingerprint login",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent intent = new Intent(LoginPage.this, MainMenu.class);
                        intent.putExtra("UserId", savedUserId);
                        intent.putExtra("UserRole", savedRole);
                        startActivity(intent);
                        finish();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Authenticate to continue")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
