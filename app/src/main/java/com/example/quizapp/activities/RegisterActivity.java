package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private SupabaseHelper supabaseHelper;

    // Password Regex: 1 Uppercase, 1 Lowercase, 1 Digit, 1 Special Char, Min 8 Length
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        supabaseHelper = new SupabaseHelper(this);
        initViews();

        btnRegister.setOnClickListener(v -> attemptRegister());

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
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void attemptRegister() {
        clearErrors();

        // Get text safely from fields
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Validate before calling Supabase
        if (!validateInputs(name, email, password, confirm)) return;

        // Disable button to prevent double-click submissions
        btnRegister.setEnabled(false);
        Toast.makeText(this, "Creating account", Toast.LENGTH_SHORT).show();

        // Call helper to register in Auth and save to 'profiles' table
        supabaseHelper.registerUser(email, password, name, new SupabaseHelper.RegistrationCallback() {
// Inside RegisterActivity.java -> attemptRegister() -> onSuccess

            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    // UPDATED MESSAGE: No more "check your email"
                    Toast.makeText(RegisterActivity.this,
                            "Registration Successful! You can now log in.",
                            Toast.LENGTH_LONG).show();

                    // Redirect to Login
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true); // Re-enable so user can fix and try again

                    // Specific check for existing users
                    if (error.toLowerCase().contains("already registered")
                            || error.toLowerCase().contains("already been registered")
                            || error.contains("422")) {
                        tilEmail.setError("This email is already registered.");
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirm) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (!Pattern.compile(PASSWORD_PATTERN).matcher(password).matches()) {
            tilPassword.setError("Min 8 chars: 1 uppercase, 1 lowercase, 1 digit, 1 special char");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirm)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}