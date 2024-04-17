package com.example.myapplication.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import com.example.myapplication.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class PairingActivity extends AppCompatActivity {
    private ListView pairingListView;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothGatt bluetoothGatt;
    private final UUID ENVIRONMENT_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
    private final UUID TEMPERATURE_CHARACTERISTIC_UUID = UUID.fromString("00002a6e-0000-1000-8000-00805f9b34fb");
    private final UUID PRESSURE_CHARACTERISTIC_UUID = UUID.fromString("0000210f-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";


    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceInfo = device.getName() + "\n" + device.getAddress();
            if (!deviceList.contains(deviceInfo) && device.getName() != null) {
                Log.d("PairingActivity", "Found device: " + deviceInfo);
                deviceList.add(deviceInfo);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
               // updateConnectionState(R.string.disconnected);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pairing);
        pairingListView = findViewById(R.id.pairingListView);
        TextView textView5 = findViewById(R.id.textView5);

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        String greeting = "Hello, " + username.substring(0, 1).toUpperCase() + username.substring(1) + "!";
        textView5.setText(greeting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // handles the click event on the list view for pairing (well really 'connecting')
        pairingListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("PairingActivity", "Item clicked: " + position);
            String deviceInfo = (String) parent.getItemAtPosition(position);
            String[] parts = deviceInfo.split("\n");
            if (parts.length == 2) {
                Log.d("PairingActivity", "Device name: " + parts[0]);
                Log.d("PairingActivity", "Device address: " + parts[1]);
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(parts[1]);
                connectDevice(device);
            }
        });

        // Setup Bluetooth and UI
        setupBluetooth();
        updatePairingUI();
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            checkPermissionsAndStartScanning();
        }
    }

    private void startScanning() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        bluetoothLeScanner.startScan(scanCallback);
        Log.d("PairingActivity", "BLE scanning started");
    }

    private void updatePairingUI() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        pairingListView.setAdapter(adapter);
    }

    private void checkPermissionsAndStartScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        } else {
            startScanning();
        }
    }

    private void connectDevice(BluetoothDevice device) {
        Log.d("PairingActivity", "Connecting to device: " + device.getAddress());
        bluetoothGatt = device.connectGatt(this, false, gattCallback, 2);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("PairingActivity", "Connected to GATT server.");
                broadcastUpdate(ACTION_GATT_CONNECTED);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("PairingActivity", "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("PairingActivity", "Services discovered: " + gatt.getServices());
                // if environment service is found, enable notifications for temperature and pressure
                if (gatt.getService(ENVIRONMENT_SERVICE_UUID) != null) {
                    enableTemperatureNotifications(gatt);
                    enablePressureNotifications(gatt);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID characteristicUUID = characteristic.getUuid();
            // temperature characteristic
            if (TEMPERATURE_CHARACTERISTIC_UUID.equals(characteristicUUID)) {
                Integer tempValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                if (tempValue != null) {
                    float temperature = tempValue / 100.0f;
                    Log.d("PairingActivity", "Temperature updated: " + temperature + "°C");
                }

                // pressure characteristic
            } else if (PRESSURE_CHARACTERISTIC_UUID.equals(characteristicUUID)) {
                Integer pressureValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                if (pressureValue != null) {
                    float pressure = pressureValue * 1.0f;
                    Log.d("PairingActivity", "Pressure updated: " + pressure + " units");
                } else {
                    Log.w("PairingActivity", "Failed to read pressure value.");
                }
            }
        }
    };

    private void enableTemperatureNotifications(BluetoothGatt gatt) {
        BluetoothGattService environmentService = gatt.getService(ENVIRONMENT_SERVICE_UUID);
        if (environmentService != null) {
            BluetoothGattCharacteristic temperatureCharacteristic = environmentService.getCharacteristic(TEMPERATURE_CHARACTERISTIC_UUID);
            if (temperatureCharacteristic != null) {
                gatt.setCharacteristicNotification(temperatureCharacteristic, true);
                BluetoothGattDescriptor descriptor = temperatureCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    private void enablePressureNotifications(BluetoothGatt gatt) {
        BluetoothGattService environmentService = gatt.getService(ENVIRONMENT_SERVICE_UUID);
        if (environmentService != null) {
            BluetoothGattCharacteristic pressureCharacteristic = environmentService.getCharacteristic(PRESSURE_CHARACTERISTIC_UUID);
            if (pressureCharacteristic != null) {
                gatt.setCharacteristicNotification(pressureCharacteristic, true);
                BluetoothGattDescriptor descriptor = pressureCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothGatt != null) {
            final boolean result = bluetoothGatt.connect();
            Log.d("PairingActivity", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("PairingActivity", "onDestroy");
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}