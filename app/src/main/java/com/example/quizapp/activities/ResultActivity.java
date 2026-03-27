package com.example.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String subject = getIntent().getStringExtra("subject");
        int correct    = getIntent().getIntExtra("correct", 0);
        int total      = getIntent().getIntExtra("total", 0);
        int wrong      = total - correct;
        double percentage = total > 0 ? (correct * 100.0 / total) : 0;

        TextView tvSubject    = findViewById(R.id.tvSubject);
        TextView tvTotal      = findViewById(R.id.tvTotal);
        TextView tvCorrect    = findViewById(R.id.tvCorrect);
        TextView tvWrong      = findViewById(R.id.tvWrong);
        TextView tvScore      = findViewById(R.id.tvScore);
        TextView tvPercentage = findViewById(R.id.tvPercentage);
        TextView tvGrade      = findViewById(R.id.tvGrade);
        Button   btnHome      = findViewById(R.id.btnHome);

        tvSubject.setText(subject);
        tvTotal.setText(String.valueOf(total));
        tvCorrect.setText(String.valueOf(correct));
        tvWrong.setText(String.valueOf(wrong));
        tvScore.setText(correct + " / " + total);
        tvPercentage.setText(String.format("%.1f%%", percentage));

        String grade;
        int gradeColor;
        if (percentage >= 90)      { grade = "Excellent! Trophy";        gradeColor = R.color.grade_excellent; }
        else if (percentage >= 75) { grade = "Great Job! Star";          gradeColor = R.color.grade_great; }
        else if (percentage >= 60) { grade = "Good! ThumbsUp";           gradeColor = R.color.grade_good; }
        else if (percentage >= 40) { grade = "Keep Practicing";          gradeColor = R.color.grade_average; }
        else                       { grade = "Needs Improvement";        gradeColor = R.color.grade_poor; }

        tvGrade.setText(grade);
        tvGrade.setTextColor(getResources().getColor(gradeColor, getTheme()));

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, SubjectSelectionActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SubjectSelectionActivity.class));
        finish();
    }
}