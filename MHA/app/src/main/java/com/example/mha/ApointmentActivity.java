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
    Button ViewBtn, BookBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int userId = getIntent().getIntExtra("UserId", -1);

        ViewBtn = findViewById(R.id.ViewBookingButton);
        BookBtn = findViewById(R.id.BookButton);

        BookBtn.setOnClickListener(v -> {
            Intent Apointintent = new Intent(ApointmentActivity.this, BookActivity.class);
            Apointintent.putExtra("UserId", userId);
            startActivity(Apointintent);
        });


    }

}