package com.example.myapplication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {
    private static final UUID UUID_ENV_SENSE_SERVICE = UUID.fromString("0000181A-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_TEMP_CHAR = UUID.fromString("00002A6E-0000-1000-8000-00805f9b34fb");
    private static final long SCAN_PERIOD = 60000; // 60 seconds

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private final Handler handler = new Handler();
    private BluetoothGatt bluetoothGatt;
    private boolean isScanning;

    public BluetoothService(Context context) {
        this.context = context;
        initializeBluetoothAdapter();
    }

    private void initializeBluetoothAdapter() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    public void scanLeDevice() {
        if (!isScanning) {
            handler.postDelayed(() -> {
                isScanning = false;
                bluetoothLeScanner.stopScan(scanCallback);
                Log.d("Bluetooth", "Stopped scanning for Bluetooth devices");
            }, SCAN_PERIOD);

            isScanning = true;
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID_ENV_SENSE_SERVICE)).build());
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            Log.d("Bluetooth", "Started scanning for Bluetooth devices.");
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        } else {
            isScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d("Bluetooth", "Stopped scanning for Bluetooth devices");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.d("Bluetooth", "Found device: " + device.getName() + ", " + device.getAddress());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("Bluetooth", "Scan failed with error code: " + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        Log.d("Bluetooth", "Connecting to GATT server");
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "Connected to GATT server.");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.");
            }
        }
    };

    public void getBluetoothDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("Paired Device", "Name: " + device.getName() + ", Address: " + device.getAddress());
            }
        } else {
            Toast.makeText(context, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }
}
