package com.example.quizapp.app.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import java.io.IOException;

public class TokenAuthenticator implements Authenticator {
    private final SessionManager sessionManager;

    public TokenAuthenticator(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // 1. Check karein ki kya hum pehle hi refresh karne ki koshish kar chuke hain?
        // Agar 3 baar se zyada fail ho chuka hai, to ruk jao (infinite loop se bachne ke liye)
        if (responseCount(response) >= 3) {
            return null;
        }

        // 2. Refresh Token nikalein SessionManager se
        String refreshToken = sessionManager.getRefreshToken();

        if (refreshToken == null) return null;

        // 3. API se naya Access Token mangne ka logic
        // Yahan aapko apni "Refresh API" ko call karna hoga (Synchronous call)
        String newAccessToken = refreshAccessTokenSync(refreshToken);

        if (newAccessToken != null) {
            // 4. Naya token mil gaya! Ise save karein
            sessionManager.saveToken(newAccessToken);

            // 5. Purani request ko naye token ke saath wapas bhein
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        }

        return null;
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private String refreshAccessTokenSync(String refreshToken) {
        // TODO: Yahan aapko naya token lane ka code likhna hai.
        // Agar aapke paas API link hai to batao, main uska code bhi likh dunga.
        return null;
    }
}