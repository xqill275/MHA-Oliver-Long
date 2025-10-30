package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainMenu extends AppCompatActivity {
    Button apointBtn, adminBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        apointBtn = findViewById(R.id.AppointmentBtn);
        adminBtn = findViewById(R.id.AdminButton);

        AppDatabase db = AppDatabase.getInstance(this);
        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");
        Log.d("MainMenu", userRole);
        Log.d("MainMenu", String.valueOf(userId));
        if (userId == -1) {
            startActivity(new Intent(MainMenu.this, MainActivity.class));
            finish();
        }

        if ("Admin".equals(userRole)) {
            adminBtn.setVisibility(View.VISIBLE);
        } else {
            adminBtn.setVisibility(View.GONE);
        }

        adminBtn.setOnClickListener(v ->
                startActivity(new Intent(MainMenu.this, AdminPage.class))
        );
    }
}