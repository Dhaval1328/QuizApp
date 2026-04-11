package com.example.quizapp.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quizapp.app.models.UserModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuizApp.db";
    private static final int DB_VERSION = 1;

    // Table: Users
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    // Table: Quiz Attempts
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
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)";

        String createAttemptsTable = "CREATE TABLE " + TABLE_ATTEMPTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_SUBJECT + " TEXT NOT NULL, " +
                COL_SCORE + " INTEGER DEFAULT 0, " +
                COL_TOTAL + " INTEGER DEFAULT 0, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_COMPLETED + " INTEGER DEFAULT 0)";

        db.execSQL(createUsersTable);
        db.execSQL(createAttemptsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTEMPTS);
        onCreate(db);
    }

    // --- AUTHENTICATION METHODS ---

    public boolean registerUser(String name, String email, String password) {
        String cleanEmail = email.toLowerCase().trim();
        if (isEmailRegistered(cleanEmail)) return false;

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(COL_NAME, name);
            cv.put(COL_EMAIL, cleanEmail);
            cv.put(COL_PASSWORD, password);
            long result = db.insert(TABLE_USERS, null, cv);
            return result != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmailRegistered(String email) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, new String[]{COL_EMAIL},
                     COL_EMAIL + "=?", new String[]{email.toLowerCase().trim()},
                     null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public UserModel loginUser(String email, String password) {
        UserModel user = null;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, null,
                     COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                     new String[]{email.toLowerCase().trim(), password},
                     null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                user = new UserModel();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            }
        }
        return user;
    }

    public boolean updatePassword(String email, String newPassword) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(COL_PASSWORD, newPassword);
            int rows = db.update(TABLE_USERS, cv, COL_EMAIL + "=?",
                    new String[]{email.toLowerCase().trim()});
            return rows > 0;
        }
    }

    // --- QUIZ LOGIC METHODS (Fixes SubjectSelection Error) ---

    public void saveAttempt(String userEmail, String subject, int score, int total) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues cv = new ContentValues();
            cv.put(COL_USER_EMAIL, userEmail.toLowerCase().trim());
            cv.put(COL_SUBJECT, subject);
            cv.put(COL_SCORE, score);
            cv.put(COL_TOTAL, total);
            cv.put(COL_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            cv.put(COL_COMPLETED, 1);
            db.insert(TABLE_ATTEMPTS, null, cv);
        }
    }

    /**
     * Re-added this method to resolve the error in SubjectSelectionActivity.
     * Returns false so users can always enter the quiz.
     */
    public boolean hasCompletedQuiz(String userEmail, String subject) {
        return false;
    }

    public int getHighScore(String userEmail, String subject) {
        int highScore = 0;
        String query = "SELECT MAX(" + COL_SCORE + ") FROM " + TABLE_ATTEMPTS +
                " WHERE " + COL_USER_EMAIL + "=? AND " + COL_SUBJECT + "=?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{userEmail.toLowerCase().trim(), subject})) {
            if (cursor != null && cursor.moveToFirst()) {
                highScore = cursor.getInt(0);
            }
        }
        return highScore;
    }
}