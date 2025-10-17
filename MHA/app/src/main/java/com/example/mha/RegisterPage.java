package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class RegisterPage extends AppCompatActivity {
    Button BackBtn, RegisterBtn;
    TextView FullName, EmailText, DateOfBirth, NHSnumber, PhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BackBtn = findViewById(R.id.Back_Button);
        RegisterBtn = findViewById(R.id.Register_button);
        FullName = findViewById(R.id.Full_Name);
        DateOfBirth = findViewById(R.id.Date_of_birth);
        NHSnumber = findViewById(R.id.NHS_Number);
        EmailText = findViewById(R.id.Email);
        PhoneNumber = findViewById(R.id.Phone_Number);

        BackBtn.setOnClickListener(v -> startActivity(new Intent(RegisterPage.this, MainActivity.class)));
        RegisterBtn.setOnClickListener(V -> ValidateForm());

        Log.e("nhsNumTest", String.valueOf(verifyNhsNum("0008700338")));
        Log.e("nhsNumTest", String.valueOf(verifyNhsNum("9434765919"))); // known valid example
    }

    public void ValidateForm() {
        String fullNameText = FullName.getText().toString().trim();
        String email = EmailText.getText().toString().trim();
        String nhsText = NHSnumber.getText().toString().trim();
        String dobText = DateOfBirth.getText().toString().trim();
        String phoneText = PhoneNumber.getText().toString().trim();
        AppDatabase db = AppDatabase.getInstance(this);

        // Empty field check
        if (TextUtils.isEmpty(fullNameText) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(nhsText) || TextUtils.isEmpty(dobText) || TextUtils.isEmpty(phoneText)) {
            Toast.makeText(this, "One or more fields are empty!", Toast.LENGTH_LONG).show();
            return;
        }

        // Email format check
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
            return;
        }
        // duplicate email check
        if (db.usersDao().getUserByEmail(email) != null) {
            Toast.makeText(this, "Email already registered!", Toast.LENGTH_LONG).show();
            return;
        }

        // NHS number length check
        if (nhsText.length() != 10) {
            Toast.makeText(this, "NHS number must be exactly 10 digits.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!nhsText.matches("\\d+")) {
            Toast.makeText(this, "NHS number must contain only digits.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!phoneText.matches("\\d+")) {
            Toast.makeText(this, "Phone number must contain only digits.", Toast.LENGTH_LONG).show();
            return;
        }

        // NHS algorithm check
        if (!verifyNhsNum(nhsText)) {
            Toast.makeText(this, "NHS number is not valid.", Toast.LENGTH_LONG).show();
            return;
        }

        // DOB format check (e.g. DD/MM/YYYY)
        if (!dobText.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            Toast.makeText(this, "Please use format DD/MM/YYYY for date of birth.", Toast.LENGTH_LONG).show();
            return;
        }

        // Phone number length check
        if (!phoneText.matches("^\\d{10,11}$")) {
            Toast.makeText(this, "Please enter a valid 10–11 digit phone number.", Toast.LENGTH_LONG).show();
            return;
        }


        // upload new users to database:

        UserEntity user = new UserEntity();
        user.fullName = fullNameText;
        user.email = email;
        user.NhsNum = nhsText;
        user.DOB = dobText;
        user.phoneNum = phoneText;

        db.usersDao().insert(user);
        Toast.makeText(this, "user registered!!!", Toast.LENGTH_LONG).show();
        List<UserEntity> users = db.usersDao().getAllUsers();
        Log.d("DB", "Users: " + users.size());

    }

    // Converts a numeric string into a list of its individual digits
    public ArrayList<Integer> getDigits(String number) {
        ArrayList<Integer> result = new ArrayList<>();
        // Loop through each character in the string
        for (int i = 0; i < number.length(); i++) {
            // Convert the character to an integer (e.g. '5' -> 5)
            result.add(number.charAt(i) - '0');
        }
        return result; // Return the list of digits
    }

    // Verifies whether an NHS number is valid according to the official check-digit algorithm
    public boolean verifyNhsNum(String nhsNumber) {
        // NHS numbers must be exactly 10 digits long
        if (nhsNumber.length() != 10) return false;

        // Convert the NHS number into a list of digits
        ArrayList<Integer> digits = getDigits(nhsNumber);

        // Remove and store the last digit (the check digit)
        int checkDigit = digits.remove(9);

        int total = 0;     // Sum of weighted digits
        int weight = 10;   // NHS algorithm uses weights 10 down to 2

        // Multiply each of the first nine digits by a decreasing weight
        for (int i = 0; i < digits.size(); i++) {
            total += digits.get(i) * weight;
            weight--;
        }

        // Compute remainder when divided by 11
        int remainder = total % 11;

        // Compute expected check digit using NHS formula
        int expectedCheck = 11 - remainder;

        // If result is 11, expected check digit should be 0
        if (expectedCheck == 11) expectedCheck = 0;

        // If result is 10, the number is invalid
        if (expectedCheck == 10) return false;

        // Valid if the expected check digit matches the actual one
        return expectedCheck == checkDigit;
    }

}
