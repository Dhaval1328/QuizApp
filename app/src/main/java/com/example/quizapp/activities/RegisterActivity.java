package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private DatabaseHelper dbHelper;

    // Password Regex: 1 Uppercase, 1 Lowercase, 1 Digit, 1 Special Char, Min 8 Length
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        initViews();

        findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());

        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
    }

    private void attemptRegister() {
        clearErrors();

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : ""; // No trim for passwords
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (validateInputs(name, email, password, confirm)) {
            performRegistration(name, email, password);
        }
    }

    private boolean validateInputs(String name, String email, String password, String confirm) {
        boolean isValid = true;

        // Name Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        }

        // Email Validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        }

        // Strict Password Validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            isValid = false;
        } else if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            tilPassword.setError("Use: 1 Uppercase, 1 Lowercase, 1 Digit, & 1 Special character");
            isValid = false;
        }

        // Confirm Password Validation
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void performRegistration(String name, String email, String password) {
        if (dbHelper.isEmailRegistered(email)) {
            tilEmail.setError("Email already registered. Please login.");
            return;
        }

        boolean isSuccess = dbHelper.registerUser(name, email, password);

        if (isSuccess) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}