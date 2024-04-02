package com.example.myapplication.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private OkHttpClient client;
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtDrName;
    private EditText txtDrEmail;
    private Button btnSaveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        client = new OkHttpClient();

        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        txtDrName = findViewById(R.id.txtDrName);
        txtDrEmail = findViewById(R.id.txtDrEmail);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);

        btnSaveInfo.setOnClickListener(v -> {
            Log.d("ProfileActivity", "First Name: " + txtFirstName.getText().toString());
            Log.d("ProfileActivity", "Last Name: " + txtLastName.getText().toString());
            Log.d("ProfileActivity", "User Email: " + txtEmail.getText().toString());
            Log.d("ProfileActivity", "Dr. Name: " + txtDrName.getText().toString());
            Log.d("ProfileActivity", "Dr. Email: " + txtDrEmail.getText().toString());

            saveProfileInformation(txtFirstName.getText().toString(), txtLastName.getText().toString(), txtEmail.getText().toString(), txtDrName.getText().toString(), txtDrEmail.getText().toString());
        });
    }

    private void saveProfileInformation(String userFirstName, String userLastName, String userEmail, String drName, String drEmail){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("firstName", userFirstName);
            jsonObject.put("lastName", userLastName);
            jsonObject.put("email", userEmail);
            jsonObject.put("drName", drName);
            jsonObject.put("drEmail", drEmail);
        }catch (JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                //.url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to save information", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "User information saved successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(ProfileActivity.this, "Failed to save information", Toast.LENGTH_SHORT).show();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
