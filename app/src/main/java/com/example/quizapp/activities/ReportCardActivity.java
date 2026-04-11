package com.example.quizapp.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.quizapp.R;

import java.util.ArrayList;

public class ReportCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build the entire UI in code — no XML layout needed for cards
        setContentView(R.layout.activity_report_card);

        // ── Get data ──────────────────────────────────────────────────────
        String subject                   = getIntent().getStringExtra("subject");
        int    correct                   = getIntent().getIntExtra("correct", 0);
        int    total                     = getIntent().getIntExtra("total", 0);
        ArrayList<String> questionTexts  = getIntent().getStringArrayListExtra("questionTexts");
        ArrayList<String> correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
        ArrayList<String> givenAnswers   = getIntent().getStringArrayListExtra("givenAnswers");

        // ── Back arrow ────────────────────────────────────────────────────
        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        // ── Summary ───────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvSubjectTitle))
                .setText(subject != null ? subject : "Quiz");
        int wrong = total - correct;
        ((TextView) findViewById(R.id.tvSummary))
                .setText("✅ " + correct + " Correct     ❌ " + wrong + " Wrong     📝 " + total + " Total");

        // ── Container ─────────────────────────────────────────────────────
        LinearLayout container = findViewById(R.id.questionContainer);

        // Safety check — show message if somehow data is missing
        if (questionTexts == null || questionTexts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No report data found.\nPlease complete a quiz first.");
            empty.setTextSize(15f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20), dp(40), dp(20), dp(20));
            empty.setTextColor(Color.parseColor("#888888"));
            container.addView(empty);
            return;
        }

        // ── One card per question ─────────────────────────────────────────
        for (int i = 0; i < questionTexts.size(); i++) {

            String qText    = questionTexts.get(i);
            String corrAns  = get(correctAnswers, i);
            String givenAns = get(givenAnswers, i);

            boolean isCorrect = !givenAns.isEmpty()
                    && givenAns.trim().equalsIgnoreCase(corrAns.trim());
            boolean isSkipped = givenAns.isEmpty();

            // Card
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cp);
            card.setRadius(dp(14));
            card.setCardElevation(dp(3));

            // Card color
            if      (isSkipped) card.setCardBackgroundColor(Color.parseColor("#FFFDE7"));
            else if (isCorrect) card.setCardBackgroundColor(Color.parseColor("#F1FAF1"));
            else                card.setCardBackgroundColor(Color.parseColor("#FFF5F5"));

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(dp(14), dp(12), dp(14), dp(14));

            // ── Q number + badge row ──────────────────────────────────────
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView qNum = new TextView(this);
            qNum.setText("Q" + (i + 1));
            qNum.setTextSize(11f);
            qNum.setTypeface(null, Typeface.BOLD);
            qNum.setTextColor(Color.WHITE);
            qNum.setGravity(Gravity.CENTER);
            qNum.setPadding(dp(10), dp(4), dp(10), dp(4));
            qNum.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    0, 1, 1f));

            TextView badge = new TextView(this);
            badge.setTextSize(11f);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(10), dp(4), dp(10), dp(4));

            if (isSkipped) {
                badge.setText("⏭  SKIPPED");
                badge.setTextColor(Color.parseColor("#E65100"));
                badge.setBackgroundColor(Color.parseColor("#FFE0B2"));
            } else if (isCorrect) {
                badge.setText("✅  CORRECT");
                badge.setTextColor(Color.parseColor("#1B5E20"));
                badge.setBackgroundColor(Color.parseColor("#C8E6C9"));
            } else {
                badge.setText("❌  WRONG");
                badge.setTextColor(Color.parseColor("#B71C1C"));
                badge.setBackgroundColor(Color.parseColor("#FFCDD2"));
            }

            topRow.addView(qNum);
            topRow.addView(spacer);
            topRow.addView(badge);
            inner.addView(topRow);

            // ── Question text ─────────────────────────────────────────────
            TextView tvQ = new TextView(this);
            LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            qp.setMargins(0, dp(10), 0, dp(10));
            tvQ.setLayoutParams(qp);
            tvQ.setText(qText);
            tvQ.setTextSize(14f);
            tvQ.setTypeface(null, Typeface.BOLD);
            tvQ.setTextColor(Color.parseColor("#212121"));
            inner.addView(tvQ);

            // Divider
            View div = new View(this);
            LinearLayout.LayoutParams dp1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
            dp1.setMargins(0, 0, 0, dp(10));
            div.setLayoutParams(dp1);
            div.setBackgroundColor(Color.parseColor("#DDDDDD"));
            inner.addView(div);

            // ── Your Answer ───────────────────────────────────────────────
            LinearLayout yourRow = new LinearLayout(this);
            yourRow.setOrientation(LinearLayout.HORIZONTAL);
            yourRow.setGravity(Gravity.TOP);

            TextView yourLabel = new TextView(this);
            yourLabel.setText("Your Answer :  ");
            yourLabel.setTextSize(13f);
            yourLabel.setTypeface(null, Typeface.BOLD);
            yourLabel.setTextColor(Color.parseColor("#555555"));

            TextView yourValue = new TextView(this);
            yourValue.setTextSize(13f);
            if (isSkipped) {
                yourValue.setText("Not Answered");
                yourValue.setTypeface(null, Typeface.ITALIC);
                yourValue.setTextColor(Color.parseColor("#E65100"));
            } else if (isCorrect) {
                yourValue.setText(givenAns + "   ✅");
                yourValue.setTypeface(null, Typeface.BOLD);
                yourValue.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                yourValue.setText(givenAns + "   ❌");
                yourValue.setTypeface(null, Typeface.BOLD);
                yourValue.setTextColor(Color.parseColor("#C62828"));
            }

            yourRow.addView(yourLabel);
            yourRow.addView(yourValue);
            inner.addView(yourRow);

            // ── Correct Answer box (wrong + skipped only) ─────────────────
            if (!isCorrect) {
                LinearLayout corrBox = new LinearLayout(this);
                corrBox.setOrientation(LinearLayout.HORIZONTAL);
                corrBox.setGravity(Gravity.TOP);
                corrBox.setPadding(dp(12), dp(10), dp(12), dp(10));
                corrBox.setBackgroundColor(Color.parseColor("#E8F5E9"));
                LinearLayout.LayoutParams cbp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cbp.setMargins(0, dp(8), 0, 0);
                corrBox.setLayoutParams(cbp);

                TextView corrLabel = new TextView(this);
                corrLabel.setText("✅  Correct Answer :  ");
                corrLabel.setTextSize(13f);
                corrLabel.setTypeface(null, Typeface.BOLD);
                corrLabel.setTextColor(Color.parseColor("#1B5E20"));

                TextView corrValue = new TextView(this);
                corrValue.setText(corrAns);
                corrValue.setTextSize(13f);
                corrValue.setTypeface(null, Typeface.BOLD);
                corrValue.setTextColor(Color.parseColor("#1B5E20"));

                corrBox.addView(corrLabel);
                corrBox.addView(corrValue);
                inner.addView(corrBox);
            }

            card.addView(inner);
            container.addView(card);
        }
    }

    private String get(ArrayList<String> list, int i) {
        return (list != null && i < list.size() && list.get(i) != null) ? list.get(i) : "";
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
