package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.Helper;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class ProfileFragment extends Fragment {
    private ApiService apiService;
    private TextView textViewUsername;
    private TextView textViewUserEmail;
    private TextView textViewDoctorName;
    private TextView textViewDoctorEmail;
    private TextView textViewDOB;
    private TextView textViewPassword;
    private Button saveProfileButton;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        apiService = ApiService.getInstance();

        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        textViewDoctorName = view.findViewById(R.id.textViewDoctorName);
        textViewDoctorEmail = view.findViewById(R.id.textViewDoctorEmail);
        textViewDOB = view.findViewById(R.id.textViewDOB);
        textViewPassword = view.findViewById(R.id.textViewPassword);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);

        saveProfileButton.setOnClickListener(v -> {
            Log.d("ProfileFragment", "User Name: " + textViewUsername.getText().toString());
            Log.d("ProfileFragment", "User Email: " + textViewUserEmail.getText().toString());
            Log.d("ProfileFragment", "Dr. Name: " + textViewDoctorName.getText().toString());
            Log.d("ProfileFragment", "Dr. Email: " + textViewDoctorEmail.getText().toString());
            Log.d("ProfileFragment", "DOB: " + textViewDOB.getText().toString());
            Log.d("ProfileFragment", "Password: " + textViewPassword.getText().toString());

            saveProfileInformation(
                    textViewDoctorName.getText().toString(),
                    textViewDoctorEmail.getText().toString(),
                    textViewDOB.getText().toString(),
                    textViewPassword.getText().toString()
            );
        });

        return view;
    }

    private void saveProfileInformation(String doctorName, String doctorEmail, String DOB, String password) {
        String token = Helper.getToken(requireContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("doctorName", doctorName);
            jsonObject.put("doctorEmail", doctorEmail);
            jsonObject.put("DOB", DOB);
            jsonObject.put("password", password);
        } catch (JSONException e){
            e.printStackTrace();
        }

        // debug
        Log.d("ProfileFragment", "Sending: " + jsonObject);

        apiService.updateProfileInformation(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("ProfileFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("ProfileFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}