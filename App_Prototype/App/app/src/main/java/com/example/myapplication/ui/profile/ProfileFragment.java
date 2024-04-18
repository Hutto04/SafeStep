package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class ProfileFragment extends Fragment {
    private Fragment currentFragment;

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

        Button saveProfileButton = view.findViewById(R.id.saveProfileButton);

        ImageView imageViewUserInfo = view.findViewById(R.id.imageViewUser);
        ImageView imageViewDoctorInfo = view.findViewById(R.id.imageViewDoctor);
        ImageView imageViewHealthRecords = view.findViewById(R.id.imageViewHealthRec);

        imageViewUserInfo.setOnClickListener(v -> loadFragment(new UserInfoFragment()));
        imageViewDoctorInfo.setOnClickListener(v -> loadFragment(new DoctorInfoFragment()));
        imageViewHealthRecords.setOnClickListener(v -> loadFragment(new HealthRecordsFragment()));

        // Default fragment to show (UserInfoFragment)
        loadFragment(new UserInfoFragment());

        saveProfileButton.setOnClickListener(v -> {
            if (currentFragment instanceof Saveable) {
                ((Saveable) currentFragment).saveData();
            }
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        // Replace the fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.profileFragmentContainer, fragment)
                .commit();
    }
}