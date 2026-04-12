package com.example.quizapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.SupabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout layoutEmail, layoutOtp, layoutNewPassword;
    private TextInputLayout tilForgotEmail, tilOtp, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etForgotEmail, etOtp, etNewPassword, etConfirmPassword;
    private MaterialButton btnSendOtp, btnVerifyOtp, btnResetPassword;

    private String userEmail;
    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        supabaseHelper = new SupabaseHelper(this);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOtp = findViewById(R.id.layoutOtp);
        layoutNewPassword = findViewById(R.id.layoutNewPassword);

        etForgotEmail = findViewById(R.id.etForgotEmail);
        etOtp = findViewById(R.id.etOtp);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilForgotEmail = findViewById(R.id.tilForgotEmail);
        tilOtp = findViewById(R.id.tilOtp);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        showStep(1);

        btnSendOtp.setOnClickListener(v -> {
            String email = etForgotEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilForgotEmail.setError("Valid email required");
                return;
            }
            userEmail = email;
            btnSendOtp.setEnabled(false);
            supabaseHelper.sendOtpToEmail(email, new SupabaseHelper.RegistrationCallback() {
                @Override
                public void onSuccess(String message) {
                    btnSendOtp.setEnabled(true);
                    showStep(2);
                }
                @Override
                public void onError(String error) {
                    btnSendOtp.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() != 6) {
                tilOtp.setError("Enter 6-digit code");
                return;
            }
            showStep(3);
        });

        btnResetPassword.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (newPass.length() < 8 || !newPass.equals(confirm)) {
                Toast.makeText(this, "Check password rules/match", Toast.LENGTH_SHORT).show();
                return;
            }

            btnResetPassword.setEnabled(false);
            supabaseHelper.verifyOtpAndResetPassword(userEmail, otp, newPass, new SupabaseHelper.RegistrationCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(ForgotPasswordActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void onError(String error) {
                    btnResetPassword.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                    showStep(2);
                }
            });
        });
    }

    private void showStep(int step) {
        layoutEmail.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutOtp.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutNewPassword.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }
}