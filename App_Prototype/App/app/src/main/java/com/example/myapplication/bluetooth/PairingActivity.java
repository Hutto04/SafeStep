package com.example.myapplication.bluetooth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class PairingActivity extends AppCompatActivity {
    private ListView pairingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pairing);
        pairingListView = findViewById(R.id.pairingListView);
        TextView textView5 = findViewById(R.id.textView5);

        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        // Retrieve the username from SharedPreferences
        String username = sharedPreferences.getString("username", "User"); // "User" is default value

        // Set the username in the TextView
        String greeting = "Hello, " + username.substring(0, 1).toUpperCase() + username.substring(1) + "!";
        textView5.setText(greeting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updatePairingUI();
    }

    private void updatePairingUI() {
        // Update the UI to show the pairing status
        // Set the list to 1, 2, 3 for now
        // TODO: make it max 5 devices
        // TODO: This is a temporary test list, will be replaced with the actual list peripheral devices (the Pico W's)
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                new String[]{"Device 1", "Device 2", "Device 3", "Device 4", "Device 5"});
        pairingListView.setAdapter(adapter);
    }
}