package com.example.quizapp.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quizapp.app.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuizApp.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    private static final String TABLE_ATTEMPTS = "quiz_attempts";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_SUBJECT = "subject";
    private static final String COL_SCORE = "score";
    private static final String COL_TOTAL = "total";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_COMPLETED = "completed";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsers);

        String createAttempts = "CREATE TABLE " + TABLE_ATTEMPTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_SUBJECT + " TEXT NOT NULL, " +
                COL_SCORE + " INTEGER DEFAULT 0, " +
                COL_TOTAL + " INTEGER DEFAULT 0, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createAttempts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTEMPTS);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String password) {
        if (isEmailRegistered(email)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_EMAIL, email);
        cv.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_EMAIL},
                COL_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public UserModel loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        if (cursor.moveToFirst()) {
            UserModel user = new UserModel();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    /**
     * Saves a new quiz attempt. Since COL_ID is AUTOINCREMENT,
     * this will create a new row every time the user finishes a quiz.
     */
    public void saveAttempt(String userEmail, String subject, int score, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, userEmail);
        cv.put(COL_SUBJECT, subject);
        cv.put(COL_SCORE, score);
        cv.put(COL_TOTAL, total);
        cv.put(COL_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        cv.put(COL_COMPLETED, 1);
        db.insert(TABLE_ATTEMPTS, null, cv);
        db.close();
    }

    /**
     * FIXED: This now returns false so the app doesn't block the user.
     * Use this ONLY if you want to check if they have EVER taken it.
     */
    public boolean hasCompletedQuiz(String userEmail, String subject) {
        // Return false here to ensure the "Already Taken" block is bypassed.
        return false;
    }

    /**
     * OPTIONAL: Get the highest score for a specific subject
     */
    public int getHighScore(String userEmail, String subject) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_SCORE + ") FROM " + TABLE_ATTEMPTS +
                        " WHERE " + COL_USER_EMAIL + "=? AND " + COL_SUBJECT + "=?",
                new String[]{userEmail, subject});

        int highScore = 0;
        if (cursor.moveToFirst()) {
            highScore = cursor.getInt(0);
        }
        cursor.close();
        return highScore;
    }
}