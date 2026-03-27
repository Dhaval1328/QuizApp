package com.example.quizapp.app.utils;

import android.content.Context;
import android.util.Log;
import com.example.quizapp.app.models.QuestionModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {

    // Now 'fileName' is passed (e.g., "java", "mobile_programming")
    public static List<QuestionModel> loadQuestions(Context context, String fileName) {
        List<QuestionModel> questions = new ArrayList<>();

        try {
            // 1. Open the specific file based on the subject clicked
            // It will look for "java.json", "python.json", etc.
            InputStream is = context.getAssets().open(fileName + ".json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            // 2. PARSE AS ARRAY DIRECTLY
            // Because your new files start with [ and not {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                QuestionModel q = new QuestionModel(
                        obj.getString("question"),
                        obj.getString("option1"),
                        obj.getString("option2"),
                        obj.getString("option3"),
                        obj.getString("option4"),
                        obj.getString("answer")
                );
                questions.add(q);
            }

            Log.d("DEBUG", "Successfully loaded " + questions.size() + " questions from " + fileName + ".json");

        } catch (Exception e) {
            Log.e("ERROR", "Error loading JSON file: " + fileName, e);
        }

        return questions;
    }
}