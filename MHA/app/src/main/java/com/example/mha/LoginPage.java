package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginPage extends AppCompatActivity {
    Button BackBtn, LoginBtn;
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
    }

    public void login() throws Exception {
        String nhsNumText = LoginNhs.getText().toString().trim();
        String emailText = LoginEmail.getText().toString().trim();
        String dobText = LoginDOB.getText().toString().trim();

        if (nhsNumText.isEmpty() || emailText.isEmpty() || dobText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }

        String emailHash = HashClass.sha256(emailText);
        String nhsHash = HashClass.sha256(nhsNumText);
        String dobHash = HashClass.sha256(dobText);

        AppDatabase db = AppDatabase.getInstance(this);
        UserEntity user = db.usersDao().getUserForLogin(emailHash, nhsHash, dobHash);

        if (user != null) {
            String decryptedName = CryptClass.decrypt(user.fullName);
            String decryptedRole = CryptClass.decrypt(user.role);
            Toast.makeText(this, "Welcome " + decryptedName + " (" + decryptedRole + ")", Toast.LENGTH_LONG).show();

            Log.d("Login", "Login successful for " + decryptedName);
            startActivity(new Intent(LoginPage.this, MainActivity.class));
        } else {
            Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
            Log.d("Login", "Login failed");
        }
    }
}
