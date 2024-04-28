package com.example.myapplication.ui.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private OkHttpClient client;
    private final String url = "http://10.0.2.2:5000/register"; // 10.0.2.2 for emulator, localhost for device

    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        client = new OkHttpClient();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button signUpButton = findViewById(R.id.signUpButton);
        TextView textViewLogin = findViewById(R.id.textViewLogin);
        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        signUpButton.setOnClickListener(v -> {
            // log username and password
            Log.d("SignupActivity", "Username: " + editTextUsername.getText().toString());
            Log.d("SignupActivity", "Password: " + editTextPassword.getText().toString());
            Log.d("SignupActivity", "Confirm Password: " + editTextConfirmPassword.getText().toString());

            // TODO: Check if the password and confirm password match
            // Register user
            registerUser(editTextUsername.getText().toString(), editTextPassword.getText().toString());
        });

        textViewLogin.setOnClickListener(v -> {
            // Navigate to the LoginActivity
            Log.d("SignupActivity", "Switch to LoginActivity");
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(SignupActivity.this, "Forgot Password", Toast.LENGTH_SHORT).show();
        });
    }

    private void registerUser(String username, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create request body with JSON object
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Correctly handle UI update on the main thread
                runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}