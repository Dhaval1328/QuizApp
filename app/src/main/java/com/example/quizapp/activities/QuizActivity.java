package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.SupabaseHelper;
import com.example.quizapp.app.models.QuestionModel;
import com.example.quizapp.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    TextView   tvTimer, tvQuestion, tvProgress, tvSubject;
    RadioGroup radioGroup;
    RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    Button      btnPrev, btnNext;
    ProgressBar progressBar;

    List<QuestionModel> questions;
    String[] userAnswers;

    int    currentIndex = 0;
    String subjectName;
    int    subjectId;

    CountDownTimer timer;
    long QUIZ_TIME = 10 * 60 * 1000; // 10 minutes

    SessionManager session;
    SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        session         = new SessionManager(this);
        supabaseHelper  = new SupabaseHelper(this);

        subjectName = getIntent().getStringExtra("subject_name");
        subjectId   = getIntent().getIntExtra("subject_id", -1);

        if (subjectName == null || subjectId == -1) {
            Toast.makeText(this, "Quiz data missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        tvTimer      = findViewById(R.id.tvTimer);
        tvQuestion   = findViewById(R.id.tvQuestion);
        tvProgress   = findViewById(R.id.tvProgress);
        tvSubject    = findViewById(R.id.tvSubject);
        radioGroup   = findViewById(R.id.radioGroup);
        rbOption1    = findViewById(R.id.rbOption1);
        rbOption2    = findViewById(R.id.rbOption2);
        rbOption3    = findViewById(R.id.rbOption3);
        rbOption4    = findViewById(R.id.rbOption4);
        btnPrev      = findViewById(R.id.btnPrev);
        btnNext      = findViewById(R.id.btnNext);
        progressBar  = findViewById(R.id.progressBar);

        tvSubject.setText(subjectName);
        setQuizUiVisible(false);
        tvQuestion.setText("Loading questions...");

        // --- NEW: Modern Back Press Logic ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });

        // Fetch Questions from Supabase
        supabaseHelper.fetchQuestions(subjectId, new SupabaseHelper.QuestionCallback() {
            @Override
            public void onSuccess(List<QuestionModel> loaded) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (loaded == null || loaded.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No questions found for " + subjectName, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                questions = loaded.size() > 20 ? loaded.subList(0, 20) : loaded;
                userAnswers = new String[questions.size()];

                setQuizUiVisible(true);
                loadQuestion(0);
                startTimer();
            }

            @Override
            public void onError(String message) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(QuizActivity.this, "Failed to load questions: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        btnPrev.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                loadQuestion(currentIndex);
            }
        });

        btnNext.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                loadQuestion(currentIndex);
            } else {
                checkAndSubmit();
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to quit? Progress will be lost.")
                .setPositiveButton("Yes", (d, w) -> finish()) // Use finish() to close the quiz
                .setNegativeButton("No", null)
                .show();
    }

    private void setQuizUiVisible(boolean visible) {
        int vis = visible ? View.VISIBLE : View.GONE;
        radioGroup.setVisibility(vis);
        btnPrev.setVisibility(vis);
        btnNext.setVisibility(vis);
        tvProgress.setVisibility(vis);
        tvTimer.setVisibility(vis);
    }

    void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        QuestionModel q = questions.get(index);
        tvQuestion.setText((index + 1) + ". " + q.getQuestion());
        rbOption1.setText(q.getOption1());
        rbOption2.setText(q.getOption2());
        rbOption3.setText(q.getOption3());
        rbOption4.setText(q.getOption4());
        tvProgress.setText((index + 1) + " / " + questions.size());
        radioGroup.clearCheck();

        if (userAnswers[index] != null) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View view = radioGroup.getChildAt(i);
                if (view instanceof RadioButton) {
                    RadioButton rb = (RadioButton) view;
                    if (rb.getText().toString().equals(userAnswers[index])) {
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        }
        btnPrev.setEnabled(index > 0);
        btnNext.setText(index == questions.size() - 1 ? "Submit" : "Next");
    }

    void saveAnswer() {
        int id = radioGroup.getCheckedRadioButtonId();
        if (id == -1) { userAnswers[currentIndex] = null; return; }
        RadioButton rb = findViewById(id);
        if (rb != null) userAnswers[currentIndex] = rb.getText().toString();
    }

    void checkAndSubmit() {
        int unanswered = 0, firstIdx = -1;
        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] == null) {
                unanswered++;
                if (firstIdx == -1) firstIdx = i;
            }
        }
        if (unanswered > 0) {
            final int target = firstIdx;
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Quiz")
                    .setMessage("You missed " + unanswered + " questions. Please answer them.")
                    .setPositiveButton("Go back", (d, w) -> {
                        currentIndex = target;
                        loadQuestion(currentIndex);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            submitQuiz();
        }
    }

    void submitQuiz() {
        if (timer != null) timer.cancel();
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            String ca = questions.get(i).getCorrectAnswer();
            if (userAnswers[i] != null && ca != null
                    && userAnswers[i].trim().equalsIgnoreCase(ca.trim())) {
                correct++;
            }
        }

        int finalCorrect = correct;
        supabaseHelper.saveQuizAttempt(subjectName, correct, questions.size(), new SupabaseHelper.RegistrationCallback() {
            @Override
            public void onSuccess(String message) {
                navigateToResults(finalCorrect);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(QuizActivity.this, "Note: Score not synced to cloud.", Toast.LENGTH_SHORT).show();
                navigateToResults(finalCorrect);
            }
        });
    }

    private void navigateToResults(int correct) {
        ArrayList<String> qTexts      = new ArrayList<>();
        ArrayList<String> corrAnsList = new ArrayList<>();
        ArrayList<String> givenAnsList = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuestionModel q = questions.get(i);
            qTexts.add(q.getQuestion());
            corrAnsList.add(q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "");
            givenAnsList.add(userAnswers[i] != null ? userAnswers[i] : "");
        }

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", correct);
        intent.putExtra("total",   questions.size());
        intent.putExtra("subject", subjectName);
        intent.putStringArrayListExtra("questionTexts",  qTexts);
        intent.putStringArrayListExtra("correctAnswers", corrAnsList);
        intent.putStringArrayListExtra("givenAnswers",   givenAnsList);
        startActivity(intent);
        finish();
    }

    void startTimer() {
        timer = new CountDownTimer(QUIZ_TIME, 1000) {
            public void onTick(long millis) {
                long min = (millis / 1000) / 60;
                long sec = (millis / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
            }
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                saveAnswer();
                for (int i = 0; i < userAnswers.length; i++)
                    if (userAnswers[i] == null) userAnswers[i] = "";
                submitQuiz();
            }
        }.start();
    }
}