package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.SettingsFragment;
import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment()); // Start with the Home Fragment - default

        // Bottom Navigation - Switch between fragments
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });


        // Start Python - to call Python functions within Java
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // chaquopy - Python SDK so that we can call Python functions within java
        Python py = Python.getInstance();
        PyObject myModule = py.getModule("test");
        PyObject myFnCallVale = myModule.get("simple_sort");
        int[] numList = {1, 3, 2, 5, 9};
        System.out.println("The Function Call's Return Value: " + myFnCallVale.call(numList).toString());
    }

    // Replace the current fragment with the new fragment - used for bottom navigation
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}