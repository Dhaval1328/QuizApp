package com.example.quizapp.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.quizapp.app.models.QuestionModel;
import com.example.quizapp.app.models.Subject;
import com.example.quizapp.app.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseHelper {

    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "SupabaseHelper";
    private final SessionManager sessionManager;
    private final Context context;

    // NEW: To prevent infinite refresh loops
    private int refreshRetryCount = 0;

    public SupabaseHelper(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    public interface RegistrationCallback {
        void onSuccess(String data);
        void onError(String message);
    }

    public interface SubjectCallback {
        void onSuccess(List<Subject> subjects);
        void onError(String message);
    }

    public interface QuestionCallback {
        void onSuccess(List<QuestionModel> questions);
        void onError(String message);
    }

    private void redirectToLogin() {
        // Reset retry count before redirecting
        refreshRetryCount = 0;
        sessionManager.saveToken(null);
        sessionManager.saveRefreshToken(null);
        try {
            Class<?> loginClass = Class.forName("com.example.quizapp.activities.LoginActivity");
            Intent intent = new Intent(context, loginClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Login activity class not found");
        }
    }

    // ─── UPDATED SILENT REFRESH LOGIC ─────────────────────────────
    private void refreshTokenAndRetry(Runnable onRefreshed) {
        // If we already tried refreshing 3 times for one action, just stop.
        if (refreshRetryCount >= 3) {
            handler.post(this::redirectToLogin);
            return;
        }

        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            handler.post(this::redirectToLogin);
            return;
        }

        refreshRetryCount++;
        Log.d(TAG, "Attempting Silent Refresh. Count: " + refreshRetryCount);

        String url = SupabaseConfig.getAuthUrl() + "/token?grant_type=refresh_token";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("refresh_token", refreshToken);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        // Use standard requestBuilder for auth actions
        Request request = SupabaseConfig.requestBuilder().url(url).post(body).build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> redirectToLogin());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JsonObject jo = gson.fromJson(responseData, JsonObject.class);

                        String newAccess = jo.get("access_token").getAsString();
                        String newRefresh = jo.get("refresh_token").getAsString();

                        sessionManager.saveToken(newAccess);
                        sessionManager.saveRefreshToken(newRefresh);

                        // Reset count on success
                        refreshRetryCount = 0;

                        // Retry the original function
                        handler.post(onRefreshed);
                    } catch (Exception e) {
                        Log.e(TAG, "Refresh parse error: " + e.getMessage());
                        handler.post(() -> redirectToLogin());
                    }
                } else {
                    Log.e(TAG, "Refresh failed with code: " + response.code());
                    handler.post(() -> redirectToLogin());
                }
            }
        });
    }

    // ─── REMAINING FUNCTIONS (UNCHANGED) ───────────────────────

    public void registerUser(String email, String password, String name, RegistrationCallback callback) {
        String url = SupabaseConfig.getAuthUrl() + "/signup";
        JsonObject metaData = new JsonObject();
        metaData.addProperty("name", name);
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);
        jsonBody.add("data", metaData);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = SupabaseConfig.requestBuilder().url(url).post(body).build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    handler.post(() -> callback.onSuccess("Registration Successful!"));
                } else {
                    try {
                        JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
                        String msg = errorJson.has("msg") ? errorJson.get("msg").getAsString() : responseBody;
                        handler.post(() -> callback.onError(msg));
                    } catch (Exception e) {
                        handler.post(() -> callback.onError(responseBody));
                    }
                }
            }
        });
    }

    public void loginUser(String email, String password, RegistrationCallback callback) {
        String url = SupabaseConfig.getAuthUrl() + "/token?grant_type=password";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = SupabaseConfig.requestBuilder().url(url).post(body).build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                        String token = jsonObject.get("access_token").getAsString();
                        String refresh = jsonObject.get("refresh_token").getAsString();

                        sessionManager.saveToken(token);
                        sessionManager.saveRefreshToken(refresh);

                        handler.post(() -> callback.onSuccess(token));
                    } catch (Exception e) {
                        handler.post(() -> callback.onError("Failed to parse login response"));
                    }
                } else {
                    handler.post(() -> callback.onError("Invalid email or password"));
                }
            }
        });
    }

    public void fetchUserProfile(String email, RegistrationCallback callback) {
        String token = sessionManager.getToken();
        String url = SupabaseConfig.getRestUrl() + "/profiles?select=name&email=eq." + email;
        Request request = SupabaseConfig.authenticatedRequestBuilder(token).url(url).get().build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError(e.getMessage())); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonArray array = gson.fromJson(response.body().string(), JsonArray.class);
                        if (array != null && array.size() > 0) {
                            String name = array.get(0).getAsJsonObject().get("name").getAsString();
                            handler.post(() -> callback.onSuccess(name));
                        } else { handler.post(() -> callback.onError("No profile found")); }
                    } catch (Exception e) { handler.post(() -> callback.onError("Failed to parse profile")); }
                } else if (response.code() == 401) {
                    refreshTokenAndRetry(() -> fetchUserProfile(email, callback));
                }
            }
        });
    }

    public void sendOtpToEmail(String email, RegistrationCallback callback) {
        String url = SupabaseConfig.getAuthUrl() + "/otp";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("create_user", false);
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = SupabaseConfig.requestBuilder().url(url).post(body).build();
        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Network Error")); }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) { handler.post(() -> callback.onSuccess("OTP Sent")); }
                else { handler.post(() -> callback.onError("Failed to send OTP")); }
            }
        });
    }

    public void verifyOtpAndResetPassword(String email, String otp, String pass, RegistrationCallback callback) {
        String url = SupabaseConfig.getAuthUrl() + "/verify";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("token", otp);
        jsonBody.addProperty("type", "recovery");
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = SupabaseConfig.requestBuilder().url(url).post(body).build();
        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Verify Error")); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JsonObject jo = gson.fromJson(response.body().string(), JsonObject.class);
                        updatePassword(pass, jo.get("access_token").getAsString(), callback);
                    } catch (Exception e) { handler.post(() -> callback.onError("Error")); }
                } else { handler.post(() -> callback.onError("Invalid OTP")); }
            }
        });
    }

    public void updatePassword(String pass, String token, RegistrationCallback callback) {
        String url = SupabaseConfig.getAuthUrl() + "/user";
        JsonObject jb = new JsonObject();
        jb.addProperty("password", pass);
        RequestBody b = RequestBody.create(jb.toString(), MediaType.get("application/json; charset=utf-8"));
        Request r = SupabaseConfig.authenticatedRequestBuilder(token).url(url).put(b).build();
        SupabaseConfig.getClient().newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Update Error")); }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) { handler.post(() -> callback.onSuccess("Updated!")); }
                else { handler.post(() -> callback.onError("Update failed")); }
            }
        });
    }

    public void fetchSubjects(SubjectCallback callback) {
        String token = sessionManager.getToken();
        String url = SupabaseConfig.getRestUrl() + "/subjects?select=*&order=name";
        Request request = SupabaseConfig.authenticatedRequestBuilder(token).url(url).get().build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Error")); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    Type t = new TypeToken<List<Subject>>() {}.getType();
                    List<Subject> subjects = gson.fromJson(response.body().string(), t);
                    handler.post(() -> callback.onSuccess(subjects != null ? subjects : new ArrayList<>()));
                } else if (response.code() == 401) {
                    refreshTokenAndRetry(() -> fetchSubjects(callback));
                } else {
                    handler.post(() -> callback.onError("Failed: " + response.code()));
                }
            }
        });
    }

    public void fetchQuestions(int subjectId, QuestionCallback callback) {
        String token = sessionManager.getToken();
        String url = SupabaseConfig.getRestUrl() + "/questions?select=*&subject_id=eq." + subjectId;
        Request request = SupabaseConfig.authenticatedRequestBuilder(token).url(url).get().build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Error")); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    JsonArray array = gson.fromJson(response.body().string(), JsonArray.class);
                    List<QuestionModel> questions = new ArrayList<>();
                    if (array != null) {
                        for (int i = 0; i < array.size(); i++) {
                            JsonObject obj = array.get(i).getAsJsonObject();
                            questions.add(new QuestionModel(obj.get("question_text").getAsString(), obj.get("option1").getAsString(), obj.get("option2").getAsString(), obj.get("option3").getAsString(), obj.get("option4").getAsString(), obj.get("correct_answer").getAsString()));
                        }
                    }
                    Collections.shuffle(questions);
                    handler.post(() -> callback.onSuccess(questions));
                } else if (response.code() == 401) {
                    refreshTokenAndRetry(() -> fetchQuestions(subjectId, callback));
                }
            }
        });
    }

    public void saveQuizAttempt(String subjectName, int score, int total, RegistrationCallback callback) {
        String token = sessionManager.getToken();
        String url = SupabaseConfig.getRestUrl() + "/quiz_attempts";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("subject_name", subjectName);
        jsonBody.addProperty("score", score);
        jsonBody.addProperty("total_questions", total);
        jsonBody.addProperty("percentage", ((double) score / total) * 100);
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = SupabaseConfig.authenticatedRequestBuilder(token).url(url).post(body).build();

        SupabaseConfig.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handler.post(() -> callback.onError("Save Failed")); }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) { handler.post(() -> callback.onSuccess("Saved")); }
                else if (response.code() == 401) {
                    refreshTokenAndRetry(() -> saveQuizAttempt(subjectName, score, total, callback));
                }
            }
        });
    }
}