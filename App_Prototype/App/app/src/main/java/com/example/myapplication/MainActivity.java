package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapplication.bluetooth.BluetoothService;
import com.example.myapplication.bluetooth.PairingFragment;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.SettingsFragment;
import com.example.myapplication.ui.debug.DebugFragment;
import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.notification.NotificationFragment;
import com.example.myapplication.ui.profile.ProfileFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static boolean SMART_SOCKS_PAIRED = false;  // Debug
    ActivityMainBinding binding;
    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setVisibility(View.GONE);
        //replaceFragment(new HomeFragment()); // Start with the Home Fragment - default
        showHomeFragment(); // Gurpreet: added showHomeFragment(); to go to homefrag
        bluetoothService = BluetoothService.getInstance(this);

        // Initialize Python - to call Python functions within Java
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        //checkBluetoothAndSetupUI(); //commented out to avoid bluetooth_connect permissions
    }

    private void checkBluetoothAndSetupUI() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Request to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            startActivityForResult(enableBtIntent, 1);
            Log.d("MainActivity", "Bluetooth is not enabled");
        } else {
            setupUIAfterBluetoothCheck();
        }
    }


    private void setupUIAfterBluetoothCheck() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH}, 1);
            Log.d("MainActivity", "Permission not granted for BLUETOOTH_CONNECT");
            return;
        }
        Log.d("MainActivity", "Permission already granted for BLUETOOTH_CONNECT, calling initializeBluetooth()");
        initializeBluetooth();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, setup the Bluetooth connection.
                Log.d("MainActivity", "Permission granted for BLUETOOTH_CONNECT, calling initializeBluetooth()");
                initializeBluetooth();
            }  else {
                Log.d("MainActivity", "Permission denied for BLUETOOTH_CONNECT");
                // ask for permission again
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH}, 1);
            }
        }
    }


    private void initializeBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, 1);
            return;
        }
        // Retrieve a list of connected Bluetooth devices
        List<BluetoothDevice> connectedDevices = bluetoothService.getConnectedDevices();
        Log.d("MainActivity", "Paired devices count: " + connectedDevices.size());

        int socksCount = 0;
        for (BluetoothDevice device : connectedDevices) {
            if (device.getName() != null && device.getName().contains("Pico")) {
                socksCount++;
            } else {
                if (device.getAddress() != null && device.getAddress().equals(bluetoothService.PICO_MAC_ADDRESS)) {
                    socksCount++;
                }
            }

            Log.d("MainActivity", "Connected device: " + (device.getName() != null ? device.getName() : "Unknown") + " " + device.getAddress());
        }

        SMART_SOCKS_PAIRED = socksCount >= 1;
        if (SMART_SOCKS_PAIRED) {
            showHomeFragment();
        } else {
            Toast.makeText(this, "Please pair and connect both SmartSocks", Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "Please pair and connect both SmartSocks");
            //Move to Pairing Activity to pair the 'socks'
            //Intent intent = new Intent(this, PairingActivity.class);
            //startActivity(intent);
            replaceFragment(new PairingFragment());
        }
    }


    private void showHomeFragment() {
        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new NotificationFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            } else if (item.getItemId() == R.id.debug) {
                replaceFragment(new DebugFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}