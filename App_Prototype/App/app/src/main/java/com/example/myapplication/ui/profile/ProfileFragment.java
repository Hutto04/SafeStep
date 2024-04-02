package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.Helper;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;

import org.json.JSONException;
import org.json.JSONObject;
public class ProfileFragment extends Fragment {
    private ApiService apiService;
    private EditText txtDoctorName;
    private EditText txtDoctorEmail;
    private EditText txtDOB;
    private EditText txtPassword;
    private Button btnSaveInfo;

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

        txtDoctorName = view.findViewById(R.id.editTextDoctorName);
        txtDoctorEmail = view.findViewById(R.id.editTextDoctorEmail);
        txtDOB = view.findViewById(R.id.editTextDOB);
        txtPassword = view.findViewById(R.id.editTextPassword2);
        btnSaveInfo = view.findViewById(R.id.saveInfoButton);

        btnSaveInfo.setOnClickListener(v -> {
            Log.d("ProfileFragment", "Doctor's Name: " + txtDoctorName.getText().toString());
            Log.d("ProfileFragment", "Doctor's Email: " + txtDoctorEmail.getText().toString());
            Log.d("ProfileFragment", "User Email: " + txtDOB.getText().toString());
            Log.d("ProfileFragment", "Dr. Name: " + txtPassword.getText().toString());

            saveProfileInformation(txtDoctorName.getText().toString(), txtDoctorEmail.getText().toString(), txtDOB.getText().toString(), txtPassword.getText().toString());
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

        apiService.postProfileInformation(token, jsonObject, new ApiService.ApiCallback() {
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