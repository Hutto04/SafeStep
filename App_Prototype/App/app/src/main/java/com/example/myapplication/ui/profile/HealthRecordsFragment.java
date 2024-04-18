package com.example.myapplication.ui.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

public class HealthRecordsFragment extends Fragment implements Saveable {

    public HealthRecordsFragment() {
        // Required empty public constructor
    }
    public static HealthRecordsFragment newInstance(String param1, String param2) {
        HealthRecordsFragment fragment = new HealthRecordsFragment();
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

        return inflater.inflate(R.layout.fragment_health_records, container, false);
    }

    @Override
    public void saveData() {
        Log.d("HealthRecordsFragment", "saveData: Saving health records");
    }
}