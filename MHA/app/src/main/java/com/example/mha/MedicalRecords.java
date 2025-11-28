package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MedicalRecords extends AppCompatActivity {
    Button UpdateRecordsBtn, viewPatientRecordsBtn, recordsBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_records);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RecordsBack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");


        UpdateRecordsBtn = findViewById(R.id.UpdatePatientRecordsBtn);
        viewPatientRecordsBtn = findViewById(R.id.ViewPatientRecordBtn);
        recordsBackBtn = findViewById(R.id.RecordsBackBtn);

        UpdateRecordsBtn.setOnClickListener(v -> {
            startActivity(new Intent(MedicalRecords.this, UpdatePatientRecords.class));
        });

        viewPatientRecordsBtn.setOnClickListener(v -> {
            startActivity(new Intent(MedicalRecords.this, ViewPatientRecords.class));
        });

        recordsBackBtn.setOnClickListener(v -> {
            Intent backIntent = new Intent(MedicalRecords.this, MainMenu.class);
            backIntent.putExtra("UserId", userId);
            backIntent.putExtra("UserRole", userRole);
            startActivity(backIntent);
        });


    }
}