package com.example.myapplication.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.Helper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private static ApiService instance;
    private final OkHttpClient client;
    private ApiService() {
        client = new OkHttpClient();
    }
    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    /**
     * This method sends a GET request to the server to get the user's data
     * @param token The token of the user
     * @param callback The callback to be called when the request is completed
     */
    public void getUserData(String token, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(Helper.URL + "/data")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure("Request failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        callback.onSuccess(responseString);
                    } else {
                        callback.onFailure("Request failed with code: " + response.code());
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * This method sends a POST request to the server to post the user's data
     * @param token The token of the user
     * @param jsonObject The JSON object containing the data to be posted (currently not being used)
     * @param callback The callback to be called when the request is completed
     */
    public void postData(String token, JSONObject jsonObject, ApiCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Set JSON media type

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // Create request body
        Request request = new Request.Builder()
                .url(Helper.URL + "/data")
                .addHeader("Authorization", "Bearer " + token) // authorization header
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure("Request failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        callback.onSuccess(responseString);
                    } else {
                        callback.onFailure("Request failed with code: " + response.code());
                    }
                } finally {
                    response.close(); // Make sure to close the response to avoid leaks
                }
            }
        });
    }

    /**
     * This method sends a GET request to the server to get the user's profile information
     * @param token The token of the user
     * @param callback The callback to be called when the request is completed
     */
    public void getProfileInformation(String token, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(Helper.URL + "/profile")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure("Request failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        Log.d("ApiService", "getProfileInformation: " + responseString);
                        callback.onSuccess(responseString);
                    } else {
                        callback.onFailure("Request failed with code: " + response.code());
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * This method sends a POST request to the server to post the user's profile information
     * @param token The token of the user
     * @param jsonObject The JSON object containing the profile information to be posted
     * @param callback The callback to be called when the request is completed
     */
    public void updateProfileInformation(String token, JSONObject jsonObject, ApiCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Set JSON media type

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // Create request body
        Request request = new Request.Builder()
                .url(Helper.URL + "/profile")
                .addHeader("Authorization", "Bearer " + token) // authorization header
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure("Request failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        callback.onSuccess(responseString);
                    } else {
                        callback.onFailure("Request failed with code: " + response.code());
                    }
                } finally {
                    response.close();
                }
            }
        });
    }


    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }
}