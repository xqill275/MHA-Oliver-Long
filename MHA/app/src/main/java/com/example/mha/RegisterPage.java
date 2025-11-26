package com.example.mha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mha.network.UserRequest;
import com.example.mha.repository.UserRepository;

import java.util.ArrayList;

public class RegisterPage extends AppCompatActivity {

    Button BackBtn, RegisterBtn;
    TextView FullName, EmailText, DateOfBirth, NHSnumber, PhoneNumber, RoleIDText;

    UserRepository userRepo;

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

        // Repository (handles online/offline sync)
        userRepo = new UserRepository(this);

        // UI references
        BackBtn = findViewById(R.id.Back_Button);
        RegisterBtn = findViewById(R.id.Register_button);
        FullName = findViewById(R.id.Full_Name);
        DateOfBirth = findViewById(R.id.Date_of_birth);
        NHSnumber = findViewById(R.id.NHS_Number);
        EmailText = findViewById(R.id.Email);
        PhoneNumber = findViewById(R.id.Phone_Number);
        RoleIDText = findViewById(R.id.RoleIDEdit);

        BackBtn.setOnClickListener(v -> startActivity(new Intent(RegisterPage.this, MainActivity.class)));

        RegisterBtn.setOnClickListener(v -> {
            try {
                validateForm();
            } catch (Exception e) {
                Log.e("Register", "Unexpected error", e);
                toast("Unexpected error: " + e.getMessage());
            }
        });
    }



    // VALIDATION + REGISTRATION

    public void validateForm() throws Exception {

        String fullNameText = FullName.getText().toString().trim();
        String emailText = EmailText.getText().toString().trim();
        String nhsText = NHSnumber.getText().toString().trim();
        String dobText = DateOfBirth.getText().toString().trim();
        String phoneText = PhoneNumber.getText().toString().trim();
        String roleInput = RoleIDText.getText().toString().trim();


        // REQUIRED FIELDS

        if (TextUtils.isEmpty(fullNameText) ||
                TextUtils.isEmpty(emailText) ||
                TextUtils.isEmpty(nhsText) ||
                TextUtils.isEmpty(dobText) ||
                TextUtils.isEmpty(phoneText)) {
            toast("One or more fields are empty!");
            return;
        }


        // EMAIL FORMAT

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            toast("Please enter a valid email address.");
            return;
        }


        // NHS VALIDATION

        if (nhsText.length() != 10 || !nhsText.matches("\\d+")) {
            toast("NHS number must be exactly 10 digits.");
            return;
        }

        if (!verifyNhsNum(nhsText)) {
            toast("Invalid NHS number.");
            return;
        }


        // DATE FORMAT (DD/MM/YYYY)

        if (!dobText.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            toast("Date of Birth must be in format DD/MM/YYYY.");
            return;
        }


        // PHONE NUMBER

        if (!phoneText.matches("^\\d{10,11}$")) {
            toast("Phone number must be 10–11 digits.");
            return;
        }


        // ROLE LOGIC

        String role = "Patient";

        if (roleInput.equals("1111")) role = "Admin";
        else if (roleInput.equals("2222")) role = "Doctor";


        // CREATE ENCRYPTED REQUEST

        UserRequest userReq = new UserRequest(
                CryptClass.encrypt(fullNameText),
                CryptClass.encrypt(emailText),
                CryptClass.encrypt(phoneText),
                CryptClass.encrypt(nhsText),
                CryptClass.encrypt(dobText),
                CryptClass.encrypt(role),
                HashClass.sha256(emailText),
                HashClass.sha256(nhsText),
                HashClass.sha256(dobText)
        );


        // SEND TO REPOSITORY (handles online/offline)

        userRepo.registerUser(userReq, new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                toast("User registered successfully!");
                startActivity(new Intent(RegisterPage.this, MainActivity.class));
            }

            @Override
            public void onFailure(String error) {
                toast("Error: " + error);
                Log.e("Register", error);
            }
        });
    }



    // NHS CHECK DIGIT ALGORITHM

    public boolean verifyNhsNum(String nhsNumber) {

        if (nhsNumber.length() != 10) return false;

        ArrayList<Integer> digits = getDigits(nhsNumber);

        int checkDigit = digits.remove(9);
        int total = 0;
        int weight = 10;

        for (int digit : digits) {
            total += digit * weight;
            weight--;
        }

        int remainder = total % 11;
        int expected = 11 - remainder;

        if (expected == 11) expected = 0;
        if (expected == 10) return false;

        return expected == checkDigit;
    }

    public ArrayList<Integer> getDigits(String number) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < number.length(); i++) {
            result.add(number.charAt(i) - '0');
        }
        return result;
    }


    // HELPERS

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
