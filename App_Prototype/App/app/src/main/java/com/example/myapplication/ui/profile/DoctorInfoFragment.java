package com.example.myapplication.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Helper;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

public class DoctorInfoFragment extends Fragment implements Saveable {
    private ApiService apiService;
    private EditText doctorName;
    private EditText doctorEmail;
    private SharedPreferences sharedPreferences;

    public DoctorInfoFragment() {}

    public static DoctorInfoFragment newInstance(String param1, String param2) {
        DoctorInfoFragment fragment = new DoctorInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor_info, container, false);
        apiService = ApiService.getInstance();

        doctorName = view.findViewById(R.id.textViewDoctorName);
        doctorEmail = view.findViewById(R.id.textViewDoctorEmail);

        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", 0);
        doctorName.setText(sharedPreferences.getString("doctor_name", ""));
        doctorEmail.setText(sharedPreferences.getString("doctor_email", ""));

        return view;
    }

    @Override
    public void saveData() {
        Log.d("DoctorInfoFragment", "saveData: Saving doctor info");
        // update shared preferences
        sharedPreferences.edit()
                .putString("doctor_name", doctorName.getText().toString())
                .putString("doctor_email", doctorEmail.getText().toString())
                .apply();

        saveProfileInformation(doctorName.getText().toString(), doctorEmail.getText().toString());
    }

    private void saveProfileInformation(String doctorName, String doctorEmail) {
        String token = Helper.getToken(requireContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("doctor_name", doctorName);
            jsonObject.put("doctor_email", doctorEmail);
        } catch (JSONException e){
            e.printStackTrace();
        }

        Log.d("DoctorInfoFragment", "Sending: " + jsonObject);

        apiService.updateProfileInformation(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("DoctorInfoFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("DoctorInfoFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}