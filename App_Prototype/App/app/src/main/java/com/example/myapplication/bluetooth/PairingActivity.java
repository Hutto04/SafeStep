package com.example.myapplication.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import java.util.ArrayList;

public class PairingActivity extends AppCompatActivity implements BluetoothService.OnDeviceFoundListener {
    private ListView pairingListView;
    private final ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothService bluetoothService;

    private static final int PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pairing);

        bluetoothService = BluetoothService.getInstance(this);
        bluetoothService.setConnectionListener(new ConnectionListener() {

            @Override
            public void onDeviceConnected() {
                runOnUiThread(() -> {
                    Toast.makeText(PairingActivity.this, "Device connected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PairingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onDeviceDisconnected() {
                runOnUiThread(() -> Toast.makeText(PairingActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onConnectionFailure() {
                runOnUiThread(() -> Toast.makeText(PairingActivity.this, "Connection failure", Toast.LENGTH_SHORT).show());
            }
        });

        pairingListView = findViewById(R.id.pairingListView);
        TextView textView5 = findViewById(R.id.textView5);

        Button rescanButton = findViewById(R.id.rescanButton);
        rescanButton.setOnClickListener(v -> {
            Log.d("PairingActivity", "Rescan button clicked");
                Log.d("PairingActivity", "Rescanning for devices");
                deviceList.clear();
                checkPermissionsAndStartScanning();
                updatePairingUI();
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        String greeting = "Hello, " + username.substring(0, 1).toUpperCase() + username.substring(1) + "!";
        textView5.setText(greeting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pairingListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("PairingActivity", "Item clicked: " + position);
            String deviceInfo = (String) parent.getItemAtPosition(position);
            String[] parts = deviceInfo.split("\n");
            if (parts.length == 2) {
                Log.d("PairingActivity", "Device name: " + parts[0]);
                Log.d("PairingActivity", "Device address: " + parts[1]);
                BluetoothDevice device = bluetoothService.getRemoteDevice(parts[1]);
                if (device != null) {
                    bluetoothService.connectToDevice(device);
                }
            } else {
                Log.w("PairingActivity", "Invalid device info: " + deviceInfo);
            }
        });


        bluetoothService.setOnDeviceFoundListener(this);
        checkPermissionsAndStartScanning();
        updatePairingUI();
    }

    private void checkPermissionsAndStartScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            bluetoothService.startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothService.startScanning();
            } else {
                Toast.makeText(this, "Location permission is required for scanning BLE devices", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePairingUI() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        pairingListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.stopScan();
        bluetoothService.closeGatt();
    }

    @Override
    public void onDeviceFound(String deviceInfo) {
        runOnUiThread(() -> {
            deviceList.add(deviceInfo);
            adapter.notifyDataSetChanged();
        });
    }
}