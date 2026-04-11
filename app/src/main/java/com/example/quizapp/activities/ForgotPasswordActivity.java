package com.example.quizapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.quizapp.R;
import com.example.quizapp.app.database.DatabaseHelper;
import com.example.quizapp.app.utils.EmailSender;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step 1 – email
    private LinearLayout layoutEmail;
    private TextInputLayout tilForgotEmail;
    private TextInputEditText etForgotEmail;
    private MaterialButton btnSendOtp;

    // Step 2 – OTP
    private LinearLayout layoutOtp;
    private TextInputLayout tilOtp;
    private TextInputEditText etOtp;
    private MaterialButton btnVerifyOtp;

    // Step 3 – new password
    private LinearLayout layoutNewPassword;
    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;

    private String generatedOtp;
    private String userEmail;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        layoutEmail      = findViewById(R.id.layoutEmail);
        tilForgotEmail   = findViewById(R.id.tilForgotEmail);
        etForgotEmail    = findViewById(R.id.etForgotEmail);
        btnSendOtp       = findViewById(R.id.btnSendOtp);

        layoutOtp        = findViewById(R.id.layoutOtp);
        tilOtp           = findViewById(R.id.tilOtp);
        etOtp            = findViewById(R.id.etOtp);
        btnVerifyOtp     = findViewById(R.id.btnVerifyOtp);

        layoutNewPassword  = findViewById(R.id.layoutNewPassword);
        tilNewPassword     = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword      = findViewById(R.id.etNewPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        btnResetPassword   = findViewById(R.id.btnResetPassword);

        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        showStep(1);

        // ── Step 1: Send OTP ──────────────────────────────────────────────
        btnSendOtp.setOnClickListener(v -> {
            tilForgotEmail.setError(null);
            String email = etForgotEmail.getText() != null
                    ? etForgotEmail.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                tilForgotEmail.setError("Email is required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilForgotEmail.setError("Enter a valid email address");
                return;
            }
            if (!dbHelper.isEmailRegistered(email)) {
                tilForgotEmail.setError("No account found with this email");
                return;
            }

            userEmail    = email;
            generatedOtp = String.format("%06d", new Random().nextInt(999999));

            btnSendOtp.setEnabled(false);
            btnSendOtp.setText("Sending…");

            new Thread(() -> {
                boolean sent = EmailSender.sendOtpEmail(email, generatedOtp);
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setText("Send OTP");
                    if (sent) {
                        Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_LONG).show();
                        showStep(2);
                    } else {
                        Toast.makeText(this,
                                "Failed to send email. Check SMTP config in EmailSender.java",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        // ── Step 2: Verify OTP ────────────────────────────────────────────
        btnVerifyOtp.setOnClickListener(v -> {
            tilOtp.setError(null);
            String entered = etOtp.getText() != null
                    ? etOtp.getText().toString().trim() : "";

            if (TextUtils.isEmpty(entered)) {
                tilOtp.setError("Enter the OTP");
                return;
            }
            if (!entered.equals(generatedOtp)) {
                tilOtp.setError("Incorrect OTP. Try again.");
                return;
            }
            showStep(3);
        });

        // ── Step 3: Reset password ────────────────────────────────────────
        btnResetPassword.setOnClickListener(v -> {
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String newPass     = etNewPassword.getText() != null
                    ? etNewPassword.getText().toString().trim() : "";
            String confirmPass = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString().trim() : "";

            if (newPass.length() < 6) {
                tilNewPassword.setError("Minimum 6 characters required");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                tilConfirmPassword.setError("Passwords do not match");
                return;
            }

            boolean updated = dbHelper.updatePassword(userEmail, newPass);
            if (updated) {
                Toast.makeText(this,
                        "Password reset successfully! Please log in.",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Something went wrong. Try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStep(int step) {
        layoutEmail.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutOtp.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutNewPassword.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }
}
