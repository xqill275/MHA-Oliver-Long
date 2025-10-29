package com.example.mha;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminPage extends AppCompatActivity {

    Spinner userSpinner, roleSpinner;
    Button updateRoleButton, addHospitalButton;
    AppDatabase db;
    List<UserEntity> users;
    List<String> userNames;
    EditText hospitalNameInput, hospitalCityInput, hospitalPostcodeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        db = AppDatabase.getInstance(this);
        userSpinner = findViewById(R.id.userSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        updateRoleButton = findViewById(R.id.updateRoleButton);

        hospitalNameInput = findViewById(R.id.hospitalNameInput);
        hospitalCityInput = findViewById(R.id.hospitalCityInput);
        hospitalPostcodeInput = findViewById(R.id.hospitalPostcodeInput);
        addHospitalButton = findViewById(R.id.addHospitalButton);

        // Fetch users
        users = db.usersDao().getAllUsers();
        userNames = new ArrayList<>();

        for (UserEntity user : users) {
            String name = CryptClass.decrypt(user.fullName);
            userNames.add(name + " (ID: " + user.uid + ")");
        }

        // Populate user spinner
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(userAdapter);

        // Populate role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Patient", "Admin"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        updateRoleButton.setOnClickListener(v -> {
            int selectedUserIndex = userSpinner.getSelectedItemPosition();
            if (selectedUserIndex < 0) {
                Toast.makeText(this, "Please select a user.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserEntity selectedUser = users.get(selectedUserIndex);
            String selectedRole = roleSpinner.getSelectedItem().toString();

            try {
                String encryptedRole = CryptClass.encrypt(selectedRole);
                db.usersDao().updateUserRole(selectedUser.uid, encryptedRole);
                Toast.makeText(this, "Role updated successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error updating role.", Toast.LENGTH_SHORT).show();
            }
        });


        addHospitalButton.setOnClickListener(v -> {
            String name = hospitalNameInput.getText().toString().trim();
            String city = hospitalCityInput.getText().toString().trim();
            String postcode = hospitalPostcodeInput.getText().toString().trim();

            if (name.isEmpty() || city.isEmpty() || postcode.isEmpty()) {
                Toast.makeText(this, "Please fill all hospital fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            HospitalEntity hospital = new HospitalEntity();
            hospital.name = name;
            hospital.city = city;
            hospital.postcode = postcode;

            db.hospitalDao().insertHospital(hospital);
            Toast.makeText(this, "Hospital added successfully!", Toast.LENGTH_SHORT).show();
            Log.d("Hospital", db.hospitalDao().getAllHospitals().toString());
            hospitalNameInput.setText("");
            hospitalCityInput.setText("");
            hospitalPostcodeInput.setText("");
        });
    }
}