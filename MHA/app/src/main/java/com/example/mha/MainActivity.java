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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button registerBtn, decryptBtn, encryptBtn;
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
        userListText = findViewById(R.id.UserListText);

        AppDatabase db = AppDatabase.getInstance(this);
        displayUsersEncrypted(db);

        // Open Register page
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterPage.class))
        );

        // Decrypt and show readable data
        decryptBtn.setOnClickListener(v -> displayUsersDecrypted(db));

        // Re-encrypt and show stored ciphertext again
        encryptBtn.setOnClickListener(v -> displayUsersEncrypted(db));
    }

    private void displayUsersEncrypted(AppDatabase db) {
        List<UserEntity> users = db.usersDao().getAllUsers();

        if (users.isEmpty()) {
            userListText.setText("No users registered yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (UserEntity user : users) {
            sb.append("Name: ").append(user.fullName)
                    .append("\nEmail: ").append(user.email)
                    .append("\nNHS: ").append(user.NhsNum)
                    .append("\nDOB: ").append(user.DOB)
                    .append("\nPhone: ").append(user.phoneNum)
                    .append("\nRole: ").append(user.role)
                    .append("\n---------------------\n");
            Log.e("Test", user.fullName);
        }
        userListText.setText(sb.toString());

    }

    private void displayUsersDecrypted(AppDatabase db) {
        List<UserEntity> users = db.usersDao().getAllUsers();


        if (users.isEmpty()) {
            userListText.setText("No users registered yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (UserEntity user : users) {
            String decryptedName = CryptClass.decrypt(user.fullName);
            String decryptedEmail = CryptClass.decrypt(user.email);
            String decryptedNhs = CryptClass.decrypt(user.NhsNum);
            String decryptedDob = CryptClass.decrypt(user.DOB);
            String decryptedPhone = CryptClass.decrypt(user.phoneNum);
            String decryptedRole = CryptClass.decrypt(user.role);

            sb.append("Name: ").append(decryptedName)
                    .append("\nEmail: ").append(decryptedEmail)
                    .append("\nNHS: ").append(decryptedNhs)
                    .append("\nDOB: ").append(decryptedDob)
                    .append("\nPhone: ").append(decryptedPhone)
                    .append("\nRole: ").append(decryptedRole)
                    .append("\n---------------------\n");
        }
        userListText.setText(sb.toString());
    }
}
