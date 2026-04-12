package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizapp.R;
import com.example.quizapp.app.SupabaseHelper;
import com.example.quizapp.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private SessionManager sessionManager;
    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            goToDashboard();
            return;
        }

        setContentView(R.layout.activity_login);
        supabaseHelper = new SupabaseHelper(this);


        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tvRegisterLink).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        }

        // Clear error messages
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Disable button to prevent multiple clicks
        btnLogin.setEnabled(false);
        btnLogin.setText("Authenticating...");

        supabaseHelper.loginUser(email, password, new SupabaseHelper.RegistrationCallback() {
            @Override
            public void onSuccess(String token) {
                // Fetch profile and start session
                fetchProfileAndStartSession(email, token);
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchProfileAndStartSession(String email, String token) {
        supabaseHelper.fetchUserProfile(email, new SupabaseHelper.RegistrationCallback() {
            @Override
            public void onSuccess(String realName) {
                // Get the refresh token that was saved in SessionManager by loginUser call
                String refreshToken = sessionManager.getRefreshToken();

                // UPDATED: Now passing 4 parameters to include refresh token
                sessionManager.createSession(realName, email, token, refreshToken);

                runOnUiThread(() -> goToDashboard());
            }
            @Override
            public void onError(String error) {
                // Get the refresh token even in fallback
                String refreshToken = sessionManager.getRefreshToken();

                // Fallback: use part of email as name if profile fetch fails
                sessionManager.createSession(email.split("@")[0], email, token, refreshToken);

                runOnUiThread(() -> goToDashboard());
            }
        });
    }

    private void goToDashboard() {
        Intent intent = new Intent(LoginActivity.this, SubjectSelectionActivity.class);
        // Clear activity stack so user can't go back to login with back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}