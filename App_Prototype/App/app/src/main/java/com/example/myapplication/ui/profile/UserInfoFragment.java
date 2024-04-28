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

public class UserInfoFragment extends Fragment implements Saveable {
    private ApiService apiService;
    private EditText userName;
    private EditText userEmail;
    private EditText userDOB;
    private EditText userWeight;
    private EditText userHeight;
    private SharedPreferences sharedPreferences;

    public UserInfoFragment() {
        // Required empty public constructor
    }

    public static UserInfoFragment newInstance(String param1, String param2) {
        UserInfoFragment fragment = new UserInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        apiService = ApiService.getInstance();

        userName = view.findViewById(R.id.textViewUserName);
        userEmail = view.findViewById(R.id.textViewUserEmail);
        userDOB = view.findViewById(R.id.textViewDOB);
        userWeight = view.findViewById(R.id.textViewWeight);
        userHeight = view.findViewById(R.id.textViewHeight);

        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", 0);
        userName.setText(sharedPreferences.getString("name", ""));
        userEmail.setText(sharedPreferences.getString("email", ""));
        userDOB.setText(sharedPreferences.getString("dob", ""));
        userWeight.setText(sharedPreferences.getString("weight", ""));
        userHeight.setText(sharedPreferences.getString("height", ""));

        return view;
    }

    @Override
    public void saveData() {
        Log.d("UserInfoFragment", "saveData: Saving user info");
        sharedPreferences.edit()
                .putString("name", userName.getText().toString())
                .putString("email", userEmail.getText().toString())
                .putString("dob", userDOB.getText().toString())
                .putString("weight", userWeight.getText().toString())
                .putString("height", userHeight.getText().toString())
                .apply();

        saveProfileInformation(userName.getText().toString(), userEmail.getText().toString(), userDOB.getText().toString(), userWeight.getText().toString(), userHeight.getText().toString());
    }

    private void saveProfileInformation(String name, String userEmail, String dob, String weight, String height) {
        String token = Helper.getToken(requireContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("email", userEmail);
            jsonObject.put("dob", dob);
            jsonObject.put("weight", weight);
            jsonObject.put("height", height);
        } catch (JSONException e){
            e.printStackTrace();
        }

        Log.d("UserInfoFragment", "Sending: " + jsonObject);

        apiService.updateProfileInformation(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("UserInfoFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("UserInfoFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}