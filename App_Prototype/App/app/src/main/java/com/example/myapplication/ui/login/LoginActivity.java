package com.example.myapplication.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Helper;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.signup.SignupActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private OkHttpClient client;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = new OkHttpClient();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.signInButton);
        textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            // log username and password
            Log.d("LoginActivity", "Username: " + username);
            Log.d("LoginActivity", "Password: " + password);

            loginUser(username, password);
        });

        textViewRegister.setOnClickListener(v -> {
            // Navigate to the SignupActivity
            Log.d("LoginActivity", "Switch to SignupActivity");
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser(String username, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Set JSON media type
        JSONObject jsonObject  = new JSONObject(); // Create new JSON Object
        try {
            jsonObject.put("username", username); // Add username to JSON object
            jsonObject.put("password", password); // Add password to JSON object
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON); // Create request body
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Helper.URL + "/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // runOnUiThread() is used to make changes to the UI from a non-UI thread (background thread) so that the UI can be updated
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String token = jsonObject.getString("token");

                        // save token to shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                        // save token to shared preferences
                        myEdit.putString("token", token);
                        // save username to shared preferences
                        myEdit.putString("username", username);
                        myEdit.apply();

                        // log token
                        Log.d("LoginActivity", "Token: " + token);

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle login failure (e.g., incorrect credentials)
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}