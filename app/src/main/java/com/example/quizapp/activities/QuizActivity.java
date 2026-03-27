package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.app.database.DatabaseHelper;
import com.example.quizapp.app.models.QuestionModel;
import com.example.quizapp.app.utils.QuestionLoader;
import com.example.quizapp.app.utils.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    TextView tvTimer, tvQuestion, tvProgress, tvSubject;
    RadioGroup radioGroup;
    RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    Button btnPrev, btnNext;

    List<QuestionModel> questions;
    String[] userAnswers;

    int currentIndex = 0;
    String subjectName; // For Display and Database
    String fileName;    // For Loading the JSON file

    CountDownTimer timer;
    long QUIZ_TIME = 10 * 60 * 1000; // 10 minutes

    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        session = new SessionManager(this);
        db = new DatabaseHelper(this);

        // Get extras from SubjectSelectionActivity
        subjectName = getIntent().getStringExtra("subject_name");
        fileName = getIntent().getStringExtra("file_name");

        if (subjectName == null || fileName == null) {
            Toast.makeText(this, "Quiz data missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind Views
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvSubject = findViewById(R.id.tvSubject);
        radioGroup = findViewById(R.id.radioGroup);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        tvSubject.setText(subjectName);

        // Load Questions using the specific fileName (e.g., "mobile_programming")
        questions = QuestionLoader.loadQuestions(this, fileName);

        if (questions == null || questions.size() == 0) {
            Toast.makeText(this, "No questions found for " + subjectName, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Shuffle and Limit to 20
        Collections.shuffle(questions);
        if (questions.size() > 20) {
            questions = questions.subList(0, 20);
        }

        userAnswers = new String[questions.size()];

        loadQuestion(0);
        startTimer();

        // Previous Button Logic
        btnPrev.setOnClickListener(v -> {
            saveAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                loadQuestion(currentIndex);
            }
        });

        // Next/Submit Button Logic
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

        // Restore user answer if they go back
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
        if (id == -1) {
            userAnswers[currentIndex] = null;
            return;
        }

        RadioButton rb = findViewById(id);
        if (rb != null) {
            userAnswers[currentIndex] = rb.getText().toString();
        }
    }

    void checkAndSubmit() {
        int unansweredCount = 0;
        int firstUnansweredIndex = -1;

        for (int i = 0; i < userAnswers.length; i++) {
            if (userAnswers[i] == null) {
                unansweredCount++;
                if (firstUnansweredIndex == -1) firstUnansweredIndex = i;
            }
        }

        if (unansweredCount > 0) {
            final int targetIndex = firstUnansweredIndex;
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Quiz")
                    .setMessage("You missed " + unansweredCount + " questions. Please answer them before submitting.")
                    .setPositiveButton("Go back", (dialog, which) -> {
                        currentIndex = targetIndex;
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
            String correctAns = questions.get(i).getCorrectAnswer();
            if (userAnswers[i] != null && correctAns != null) {
                if (userAnswers[i].trim().equalsIgnoreCase(correctAns.trim())) {
                    correct++;
                }
            }
        }

        // Save to DB using subjectName (e.g. "Mobile Programming")
        db.saveAttempt(session.getEmail(), subjectName, correct, questions.size());

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", correct);
        intent.putExtra("total", questions.size());
        intent.putExtra("subject", subjectName);

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
                submitQuiz();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (currentIndex > 0) {
            saveAnswer();
            currentIndex--;
            loadQuestion(currentIndex);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Quiz")
                    .setMessage("Are you sure you want to quit? Your progress will be lost.")
                    .setPositiveButton("Yes", (d, w) -> super.onBackPressed())
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}