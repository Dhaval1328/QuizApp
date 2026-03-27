package com.example.quizapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.utils.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SessionManager session = new SessionManager(SplashActivity.this);

            Intent intent;

            if (session.isLoggedIn()) {

                intent = new Intent(SplashActivity.this, SubjectSelectionActivity.class);

            } else {

                intent = new Intent(SplashActivity.this, LoginActivity.class);

            }

            startActivity(intent);

            finish();

        },2000);

    }

}