package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // ── Score data ────────────────────────────────────────────────────
        String subject    = getIntent().getStringExtra("subject");
        int    correct    = getIntent().getIntExtra("correct", 0);
        int    total      = getIntent().getIntExtra("total", 0);
        int    wrong      = total - correct;
        double percentage = total > 0 ? (correct * 100.0 / total) : 0;

        // ── Question arrays (passed from QuizActivity) ────────────────────
        ArrayList<String> questionTexts  = getIntent().getStringArrayListExtra("questionTexts");
        ArrayList<String> correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
        ArrayList<String> givenAnswers   = getIntent().getStringArrayListExtra("givenAnswers");

        // ── Bind views ────────────────────────────────────────────────────
        TextView tvSubject    = findViewById(R.id.tvSubject);
        TextView tvTotal      = findViewById(R.id.tvTotal);
        TextView tvCorrect    = findViewById(R.id.tvCorrect);
        TextView tvWrong      = findViewById(R.id.tvWrong);
        TextView tvScore      = findViewById(R.id.tvScore);
        TextView tvPercentage = findViewById(R.id.tvPercentage);
        TextView tvGrade      = findViewById(R.id.tvGrade);
        Button   btnHome      = findViewById(R.id.btnHome);
        Button   btnShowReport = findViewById(R.id.btnShowReport);

        // ── Fill data ─────────────────────────────────────────────────────
        tvSubject.setText(subject);
        tvTotal.setText(String.valueOf(total));
        tvCorrect.setText(String.valueOf(correct));
        tvWrong.setText(String.valueOf(wrong));
        tvScore.setText(correct + " / " + total);
        tvPercentage.setText(String.format("%.1f%%", percentage));

        // ── Grade ─────────────────────────────────────────────────────────
        String grade;
        int gradeColor;
        if      (percentage >= 90) { grade = "Excellent! Trophy"; gradeColor = R.color.grade_excellent; }
        else if (percentage >= 75) { grade = "Great Job! Star";   gradeColor = R.color.grade_great;     }
        else if (percentage >= 60) { grade = "Good! ThumbsUp";    gradeColor = R.color.grade_good;      }
        else if (percentage >= 40) { grade = "Keep Practicing";   gradeColor = R.color.grade_average;   }
        else                       { grade = "Needs Improvement"; gradeColor = R.color.grade_poor;      }

        tvGrade.setText(grade);
        tvGrade.setTextColor(getResources().getColor(gradeColor, getTheme()));

        // ── Home button ───────────────────────────────────────────────────
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, SubjectSelectionActivity.class));
            finish();
        });

        // ── Show Report button ────────────────────────────────────────────
        btnShowReport.setOnClickListener(v -> {
            Intent i = new Intent(this, ReportCardActivity.class);
            i.putExtra("subject", subject);
            i.putExtra("correct", correct);
            i.putExtra("total",   total);
            i.putStringArrayListExtra("questionTexts",  questionTexts);
            i.putStringArrayListExtra("correctAnswers", correctAnswers);
            i.putStringArrayListExtra("givenAnswers",   givenAnswers);
            startActivity(i);
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SubjectSelectionActivity.class));
        finish();
    }
}
