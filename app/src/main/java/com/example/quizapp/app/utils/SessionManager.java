package com.example.quizapp.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "QuizAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token"; // Added for silent login

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Creates a login session (Updated to include refresh token)
     */
    public void createSession(String name, String email, String token, String refreshToken) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken); // Saving refresh token
        editor.apply();
    }

    /**
     * Updates only the access token if it refreshes
     */
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        if (token == null) {
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
        }
        editor.apply();
    }

    /**
     * NEW: Updates only the refresh token
     */
    public void saveRefreshToken(String refreshToken) {
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * NEW: Get refresh token for background refresh
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "User");
    }

    /**
     * Clears all session data (Logout)
     */
    public void logout() {
        editor.clear();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}