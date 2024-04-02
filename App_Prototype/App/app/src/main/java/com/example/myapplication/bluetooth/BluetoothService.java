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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.myapplication.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {
    private static final UUID UUID_ENV_SENSE_SERVICE = UUID.fromString("0000181A-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_TEMP_CHAR = UUID.fromString("00002A6E-0000-1000-8000-00805f9b34fb");


    // Duration for isScanning (in milliseconds)
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
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }
        
    }

    public void scanLeDevice() {
        if (!isScanning) {
            //Stops isScanning after a predefined scan period.
            handler.postDelayed(() -> {
                isScanning = false;
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothLeScanner.stopScan(scanCallback);
                Log.d("Bluetooth", "Stopped scanning for Bluetooth devices after " + SCAN_PERIOD + " milliseconds");
            }, SCAN_PERIOD);

            isScanning = true;

            // Scan for devices advertising the environmental sensing service
            // This was for testing.. its not being used right now but you can add the filter scan in the startScan method, its currently null (line 283)
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID_ENV_SENSE_SERVICE))
                    .build();

            List<ScanFilter> filters = new ArrayList<>();
            filters.add(scanFilter);

            // Scan settings - low latency mode
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Bluetooth", "Started isScanning for Bluetooth devices.....");

            // startScan - (filters, settings, callback)
            bluetoothLeScanner.startScan(filters, settings, scanCallback);

            //bluetoothLeScanner.startScan(scanCallback);
        } else {
            isScanning = false;
            Log.d("Bluetooth", "Stopped isScanning for Bluetooth devices");
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    // Device scan callback.
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Bluetooth", "Found device: " + device.getName() + ", " + device.getAddress());
            Toast.makeText(context, "Found device: " + device.getName() + ", " + device.getAddress(), Toast.LENGTH_SHORT).show();
            if (device.getName() != null) {
                Log.d("Bluetooth", "Connecting to device: " + device.getName() + ", " + device.getAddress());
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("Bluetooth", "Scan failed with error code: " + errorCode);
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Bluetooth", "Connecting to GATT server");
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    // Bluetooth GATT callback
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "Connected to GATT server.");
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothGatt.discoverServices();
                Log.d("Bluetooth", "Discovering services");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService envSensingService = bluetoothGatt.getService(UUID_ENV_SENSE_SERVICE);
                if (envSensingService != null) {
                    BluetoothGattCharacteristic tempCharacteristic = envSensingService.getCharacteristic(UUID_TEMP_CHAR);
                    if (tempCharacteristic != null) {
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        bluetoothGatt.setCharacteristicNotification(tempCharacteristic, true);
                        BluetoothGattDescriptor descriptor = tempCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                        Log.d("Bluetooth", "Subscribed to temperature characteristic");
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(UUID_TEMP_CHAR)) {
                byte[] tempData = characteristic.getValue();
                // Process the received temperature data
                Log.d("Bluetooth", "Received temperature data: " + new String(tempData));
            }
        }
    };

    public void getBluetoothDevices() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            // List paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d("Paired Device", "Name: " + device.getName() + ", Address: " + device.getAddress());
                // Consider showing these devices in a UI element
            }
        } else {
            Toast.makeText(context, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        }
    }
}

