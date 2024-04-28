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
import com.example.myapplication.api.ApiService;
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
    private ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = new OkHttpClient();

        apiService = ApiService.getInstance();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.signInButton);
        textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            Log.d("LoginActivity", "Username: " + username);
            Log.d("LoginActivity", "Password: " + password);

            loginUser(username, password);
        });

        textViewRegister.setOnClickListener(v -> {
            Log.d("LoginActivity", "Switch to SignupActivity");
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser(String username, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject  = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
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
                        myEdit.putString("token", token);
                        myEdit.putString("username", username);
                        myEdit.apply();

                        Log.d("LoginActivity", "Token: " + token);

                        getProfileInformation(token);

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
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getProfileInformation(String token) {
        apiService.getProfileInformation(token, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String responseString) {
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    Log.d("LoginActivity", "Full JSON Response: " + jsonObject.toString(2));

                    String username = jsonObject.optString("username", "");
                    String email = jsonObject.optString("email", "");
                    String dob = jsonObject.optString("dob", "");
                    String weight = jsonObject.optString("weight", "");
                    String height = jsonObject.optString("height", "");
                    String doctorName = jsonObject.optString("doctor_name", "");
                    String doctorEmail = jsonObject.optString("doctor_email", "");

                    // Save profile info to shared preferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("username", username);
                    myEdit.putString("email", email);
                    myEdit.putString("dob", dob);
                    myEdit.putString("weight", weight);
                    myEdit.putString("height", height);
                    myEdit.putString("doctor_name", doctorName);
                    myEdit.putString("doctor_email", doctorEmail);
                    myEdit.apply();

                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Profile loaded", Toast.LENGTH_SHORT).show());
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to parse profile data", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("LoginActivity", "Profile load error: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show());
            }
        });
    }
}