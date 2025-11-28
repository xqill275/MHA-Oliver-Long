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

import com.example.mha.database.AppDatabase;


public class MainMenu extends AppCompatActivity {
    Button apointBtn, adminBtn, recordBtn, logOutBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RecordsBack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        apointBtn = findViewById(R.id.AppointmentBtn);
        adminBtn = findViewById(R.id.AdminButton);
        recordBtn = findViewById(R.id.RecordsBtn);
        logOutBtn = findViewById(R.id.LogOutBtn);

        AppDatabase db = AppDatabase.getInstance(this);
        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");
        //Log.d("MainMenu", userRole);
        //Log.d("MainMenu", String.valueOf(userId));
        if (userId == -1) {
            startActivity(new Intent(MainMenu.this, MainActivity.class));
            finish();
        }



        if ("Admin".equals(userRole)) {
            adminBtn.setVisibility(View.VISIBLE);
            apointBtn.setVisibility(View.VISIBLE);
        } else {
            adminBtn.setVisibility(View.GONE);
        }

        if ("Doctor".equals(userRole)) {
            recordBtn.setVisibility(View.VISIBLE);
            apointBtn.setVisibility(View.VISIBLE);
        } else {
            recordBtn.setVisibility(View.GONE);
        }

        if ("Patient".equals(userRole)) {
            apointBtn.setVisibility(View.VISIBLE);
        }



        apointBtn.setOnClickListener(v -> {
            Intent Apointintent = new Intent(MainMenu.this, ApointmentActivity.class);
            Apointintent.putExtra("UserId", userId);
            Apointintent.putExtra("UserRole", userRole);
            startActivity(Apointintent);
        });
        adminBtn.setOnClickListener(v -> {
            Intent adminIntent = new Intent(MainMenu.this, AdminPage.class);
            adminIntent.putExtra("UserId", userId);
            adminIntent.putExtra("UserRole", userRole);
            startActivity(adminIntent);
        });
        recordBtn.setOnClickListener(v -> {
            Intent recordIntent = new Intent(MainMenu.this, MedicalRecords.class);
            recordIntent.putExtra("UserId", userId);
            recordIntent.putExtra("UserRole", userRole);
            startActivity(recordIntent);
        });


        logOutBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, MainActivity.class));
            finish();
        });
    }

}