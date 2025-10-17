package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button registerBtn;
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
        userListText = findViewById(R.id.UserListText);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterPage.class));

            }
        });

        AppDatabase db = AppDatabase.getInstance(this);
        List<UserEntity> users = db.usersDao().getAllUsers();

        if (users.isEmpty()) {
            userListText.setText("No users registered yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (UserEntity user : users) {
                sb.append("Name: ").append(user.fullName)
                        .append("\nEmail: ").append(user.email)
                        .append("\nNHS: ").append(user.NhsNum)
                        .append("\nDOB: ").append(user.DOB)
                        .append("\nPhone: ").append(user.phoneNum)
                        .append("\n---------------------\n");
            }
            userListText.setText(sb.toString());
        }
    }
}
