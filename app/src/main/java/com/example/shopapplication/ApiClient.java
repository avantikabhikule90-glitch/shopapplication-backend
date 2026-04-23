package com.example.shopapplication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "https://shopapplication-backend.onrender.com";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void post(String endpoint, String jsonBody, Callback callback) {
        executor.execute(() -> {
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String rb = response.body() != null ? response.body().string() : "";
                callback.onSuccess(rb);
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static void get(String endpoint, Callback callback) {
        executor.execute(() -> {
            Request request = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String rb = response.body() != null ? response.body().string() : "";
                callback.onSuccess(rb);
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        });
    }
}