package com.example.quizapp.app;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SupabaseConfig {

    // ── PASTE YOUR VALUES HERE ────────────────────────────────
    private static final String SUPABASE_URL =
            "https://mgvvzhxrfbhridsyvnhk.supabase.co";

    private static final String SUPABASE_KEY =
            "sb_publishable_GeOSUCXPie6H7fUiE9G6fQ_YpqcKZYu";
    // ─────────────────────────────────────────────────────────

    private static final OkHttpClient client = new OkHttpClient();

    /** Base URL for Supabase REST API (Tables) */
    public static String getRestUrl() {
        return SUPABASE_URL + "/rest/v1";
    }

    /** Base URL for Supabase Auth (Login/Signup) */
    public static String getAuthUrl() {
        return SUPABASE_URL + "/auth/v1";
    }

    /** * Default builder using the Anon Key.
     * Use this for: Registration, Login, and fetching Public Questions.
     */
    public static Request.Builder requestBuilder() {
        return new Request.Builder()
                .addHeader("apikey",        SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type",  "application/json");
    }

    /** * AUTHENTICATED builder using a specific User Token.
     * Use this for: Saving Quiz Results, Updating Profile, and fetching Private Subjects.
     */
    public static Request.Builder authenticatedRequestBuilder(String userToken) {
        // If the session is missing a token, we must not attempt an auth call
        // This will allow the SupabaseHelper to catch the error and redirect to login
        if (userToken == null || userToken.isEmpty()) {
            return requestBuilder();
        }

        return new Request.Builder()
                .addHeader("apikey",        SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + userToken)
                .addHeader("Content-Type",  "application/json");
    }

    public static OkHttpClient getClient() {
        return client;
    }
}