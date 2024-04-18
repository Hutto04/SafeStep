package com.example.myapplication.bluetooth;

import android.Manifest;
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
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService {
    private static BluetoothService instance;
    private float latestTemperature;
    private float latestPressures;
    private static final UUID UUID_ENV_SENSE_SERVICE = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_TEMP_CHAR = UUID.fromString("00002a6e-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_PRESSURE_CHAR = UUID.fromString("0000210f-0000-1000-8000-00805f9b34fb");

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    public boolean isScanning;
    private final List<String> deviceList = new ArrayList<>();
    private OnDeviceFoundListener onDeviceFoundListener;

    public interface OnDeviceFoundListener {
        void onDeviceFound(String deviceInfo);
    }

    private BluetoothService(Context context) {
        this.context = context;
        initializeBluetoothAdapter();
    }

    // Singleton
    public static BluetoothService getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothService(context);
        }
        return instance;
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

    public BluetoothDevice getRemoteDevice(String address) {
        return bluetoothAdapter.getRemoteDevice(address);
    }

    public void startScanning() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (isScanning) {
            Log.d("BluetoothService", "Already scanning for Bluetooth devices.. stopping scan");
            stopScan();
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")))
                .setDeviceName("Pico") // Replace with your device name
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        //bluetoothLeScanner.startScan(filters, settings, scanCallback);
        Log.d("BluetoothService", "BLE scanning started");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BluetoothService", "Bluetooth scan permission not granted");
            // ask for permission
            ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
            return;
        }
        bluetoothLeScanner.startScan(scanCallback);
        isScanning = true;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "ScanCallback - Bluetooth permission not granted");
                ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            String deviceInfo = device.getName() + "\n" + device.getAddress();
            if (!deviceList.contains(deviceInfo) && device.getName() != null) {
                Log.d("Bluetooth", "Found device: " + deviceInfo);
                deviceList.add(deviceInfo);
                if (onDeviceFoundListener != null) {
                    onDeviceFoundListener.onDeviceFound(deviceInfo);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("Bluetooth", "Scan failed with error code: " + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Bluetooth", "connectToDevice - Bluetooth connect permission not granted");
            ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
            return;
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback, 2);
        Log.d("Bluetooth", "Connecting to GATT server");
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth", "Connected to GATT server.");
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("Bluetooth", "onConnectionStateChange - Bluetooth connect permission not granted");
                    ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                    return;
                }
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Services discovered: " + gatt.getServices());
                // if environment service is found, enable notifications for temperature and pressure
                BluetoothGattService envSenseService = gatt.getService(UUID_ENV_SENSE_SERVICE);
                if (envSenseService != null) {
                    enableCharacteristicNotification(gatt, envSenseService, UUID_TEMP_CHAR);
                    enableCharacteristicNotification(gatt, envSenseService, UUID_PRESSURE_CHAR);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID characteristicUUID = characteristic.getUuid();
            if (UUID_TEMP_CHAR.equals(characteristicUUID)) {
                Integer tempValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                if (tempValue != null) {
                    float temperature = tempValue / 100.0f;
                    Log.d("Bluetooth", "Temperature updated: " + temperature + "°C");
                }
            } else if (UUID_PRESSURE_CHAR.equals(characteristicUUID)) {
                Integer pressureValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
                if (pressureValue != null) {
                    float pressure = pressureValue / 1000.0f;  // Convert from Pascals to kilopascals (kPa)?
                    Log.d("Bluetooth", "Pressure updated: " + pressure + " kPa");
                } else {
                    Log.w("Bluetooth", "Failed to read pressure value.");
                }
            }
        }
    };

    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "enableCharacteristicNotification - Bluetooth connect permission not granted");
                ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "enableCharacteristicNotification - Bluetooth connect permission not granted");
                ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            gatt.writeDescriptor(descriptor);
        }
    }

    public void stopScan() {
        if (!isScanning) {
            Log.w("BluetoothService", "No scan to stop");
            return;
        }
        if (bluetoothLeScanner != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("BluetoothService", "stopScan - Bluetooth scan permission not granted");
                ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d("BluetoothService", "Stopped scanning for Bluetooth devices");
            isScanning = false;
        }
    }

    public void closeGatt() {
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "closeGatt - Bluetooth connect permission not granted");
                ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                return;
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public void setOnDeviceFoundListener(OnDeviceFoundListener listener) {
        this.onDeviceFoundListener = listener;
    }

    public float getLatestTemperature() {
        return latestTemperature;
    }

    public float getLatestPressure() {
        return latestPressures;
    }
}
