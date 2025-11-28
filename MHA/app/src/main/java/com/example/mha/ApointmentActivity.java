package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ApointmentActivity extends AppCompatActivity {
    Button ViewBtn, BookBtn, BackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RecordsBack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int userId = getIntent().getIntExtra("UserId", -1);
        String userRole = getIntent().getStringExtra("UserRole");


        ViewBtn = findViewById(R.id.ViewBookingButton);
        BookBtn = findViewById(R.id.BookButton);
        BackBtn = findViewById(R.id.BookingBackBtn);

        BookBtn.setOnClickListener(v -> {
            Intent Apointintent = new Intent(ApointmentActivity.this, BookActivity.class);
            Apointintent.putExtra("UserId", userId);
            Apointintent.putExtra("UserRole", userRole);
            startActivity(Apointintent);
        });

        ViewBtn.setOnClickListener(v -> {
            Intent Apointintent = new Intent(ApointmentActivity.this, ViewBookingActivity.class);
            Apointintent.putExtra("UserId", userId);
            Apointintent.putExtra("UserRole", userRole);
            startActivity(Apointintent);
        });

        BackBtn.setOnClickListener(v -> {
            Intent Apointintent = new Intent(ApointmentActivity.this, MainMenu.class);
            Apointintent.putExtra("UserId", userId);
            Apointintent.putExtra("UserRole", userRole);
            startActivity(Apointintent);
        });



    }

}