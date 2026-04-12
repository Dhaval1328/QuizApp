package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quizapp.R;
import com.example.quizapp.app.SupabaseHelper;
import com.example.quizapp.app.models.Subject;
import com.example.quizapp.app.utils.SessionManager;

import java.util.List;

public class SubjectSelectionActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SupabaseHelper supabaseHelper;

    // Card IDs from your XML layout
    private final int[] cardIds = {
            R.id.cardJava, R.id.cardPython, R.id.cardCpp, R.id.cardMobile,
            R.id.cardC, R.id.cardDotNet, R.id.cardCsharp, R.id.cardJS, R.id.cardRdbms, R.id.cardComputerNetwork
    };

    // Names used to match with Supabase database rows
    private final String[] subjectNames = {
            "Java", "Python", "C++", "Mobile Programming",
            "C Programming", ".NET", "C#", "JavaScript", "RDBMS","Computer Network"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        sessionManager = new SessionManager(this);
        supabaseHelper = new SupabaseHelper(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + sessionManager.getName() + "!");

        // Initially disable cards while loading data from Supabase
        setCardsEnabled(false);

        loadSubjectsFromSupabase();
    }

    private void loadSubjectsFromSupabase() {
        supabaseHelper.fetchSubjects(new SupabaseHelper.SubjectCallback() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                if (subjects.isEmpty()) {
                    Toast.makeText(SubjectSelectionActivity.this, "No subjects found in database!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Match UI Cards to Database Subjects
                for (int i = 0; i < cardIds.length; i++) {
                    CardView card = findViewById(cardIds[i]);
                    if (card == null) continue;

                    String cardSubjectName = subjectNames[i];
                    Subject matched = findSubject(subjects, cardSubjectName);

                    if (matched != null) {
                        final Subject finalSubject = matched;
                        card.setEnabled(true);
                        card.setAlpha(1f);
                        card.setOnClickListener(v -> startQuiz(finalSubject));
                    } else {
                        // Subject doesn't exist in Supabase table yet - dim it
                        card.setEnabled(false);
                        card.setAlpha(0.4f);
                    }
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SubjectSelectionActivity.this, "Sync Error: " + message, Toast.LENGTH_LONG).show();
                setCardsEnabled(false);
            }
        });
    }

    private Subject findSubject(List<Subject> subjects, String name) {
        for (Subject s : subjects) {
            if (s.getName().equalsIgnoreCase(name.trim())) {
                return s;
            }
        }
        return null;
    }

    private void setCardsEnabled(boolean enabled) {
        for (int id : cardIds) {
            CardView card = findViewById(id);
            if (card != null) {
                card.setEnabled(enabled);
                card.setAlpha(enabled ? 1f : 0.5f);
            }
        }
    }

    private void startQuiz(Subject subject) {
        // We removed the local dbHelper check.
        // The quiz attempt is now saved to Supabase at the end of QuizActivity.
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("subject_name", subject.getName());
        intent.putExtra("subject_id",   subject.getId());
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