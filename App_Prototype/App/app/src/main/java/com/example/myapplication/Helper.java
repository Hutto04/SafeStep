package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class Helper {
    public static final String URL = "https://safestep.onrender.com/"; // Server URL - http://10.0.2.2:5000/ for emulator - for prod: https://safestep.onrender.com/
    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }
}
