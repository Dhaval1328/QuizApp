package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.quizapp.R;
import com.example.quizapp.app.database.DatabaseHelper;
import com.example.quizapp.app.utils.SessionManager;

public class SubjectSelectionActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    // These names must match what you want to show on screen
    private final String[] subjects = {
            "Java", "Python", "C++", "Mobile Programming",
            "C Programming", ".NET", "C#", "JavaScript", "RDBMS"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + sessionManager.getName() + "!");

        // Card IDs from your XML layout
        int[] cardIds = {
                R.id.cardJava, R.id.cardPython, R.id.cardCpp, R.id.cardMobile,
                R.id.cardC, R.id.cardDotNet, R.id.cardCsharp, R.id.cardJS, R.id.cardRdbms
        };

        for (int i = 0; i < cardIds.length; i++) {
            final String subject = subjects[i];
            CardView card = findViewById(cardIds[i]);
            if (card != null) {
                card.setOnClickListener(v -> startQuiz(subject));
            }
        }
    }

    private void startQuiz(String subject) {
        // 1. Check if user already finished this specific subject
        if (dbHelper.hasCompletedQuiz(sessionManager.getEmail(), subject)) {
            new AlertDialog.Builder(this)
                    .setTitle("Quiz Already Taken")
                    .setMessage("You have already completed the " + subject + " quiz.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // 2. Convert display name to a safe filename
        // Example: "Mobile Programming" -> "mobile_programming"
        // Example: "C++" -> "cpp"
        String fileName = subject.toLowerCase()
                .replace(" ", "_")
                .replace("++", "pp")
                .replace("#", "sharp")
                .replace(".", "");

        // 3. Pass both names to the QuizActivity
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("subject_name", subject);
        intent.putExtra("file_name", fileName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}