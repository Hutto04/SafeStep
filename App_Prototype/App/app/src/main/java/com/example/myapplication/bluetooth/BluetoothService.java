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

import com.example.myapplication.Helper;
import com.example.myapplication.api.ApiService;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BluetoothService {
    private static BluetoothService instance;
    private ApiService apiService;
    private ConnectionListener connectionListener;
    private float[] latestTemperatures;
    private float[] latestPressures;
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
    public final String PICO_MAC_ADDRESS = "28:CD:C1:06:FC:42";

    public interface OnDeviceFoundListener {
        void onDeviceFound(String deviceInfo);
    }

    private BluetoothService(Context context) {
        this.context = context;
        initializeBluetoothAdapter();
        apiService = ApiService.getInstance();
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
            if (!deviceList.contains(deviceInfo) && device.getAddress().equals(PICO_MAC_ADDRESS)) {
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

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
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
                gatt.requestMtu(50); // Request higher MTU for faster data transfer - default is 23 bytes
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("Bluetooth", "onConnectionStateChange - Bluetooth connect permission not granted");
                    ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                    return;
                }
                bluetoothGatt.discoverServices();
                if (connectionListener != null) {
                    connectionListener.onDeviceConnected();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Disconnected from GATT server.");
                if (connectionListener != null) {
                    connectionListener.onDeviceDisconnected();
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d("Bluetooth", "MTU size changed to: " + mtu);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("Bluetooth", "onMtuChanged - Bluetooth connect permission not granted");
                    ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
                    return;
                }
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Services discovered: " + gatt.getServices());
                // if environment service is found, enable notifications for temperature and pressure
                BluetoothGattService envSenseService = gatt.getService(UUID_ENV_SENSE_SERVICE);
                if (envSenseService != null) {
                    Log.d("Bluetooth", "Environment service found");
                    enableCharacteristicNotification(gatt, envSenseService, UUID_TEMP_CHAR);
                    enableCharacteristicNotification(gatt, envSenseService, UUID_PRESSURE_CHAR);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID characteristicUUID = characteristic.getUuid();
            if (UUID_TEMP_CHAR.equals(characteristicUUID)) {
                byte[] tempBytes = characteristic.getValue();
                if (tempBytes != null) {
                    int numFloats = tempBytes.length / 4;
                    float[] temperatures = new float[numFloats];
                    ByteBuffer.wrap(tempBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(temperatures);
                    latestTemperatures = temperatures;
                    updateTemperature(temperatures);
                    Log.d("Bluetooth", "Temperatures updated: " + Arrays.toString(temperatures) + "°C");
                } else {
                    Log.d("Bluetooth", "Temperature byte array is null");
                }
            } else if (UUID_PRESSURE_CHAR.equals(characteristicUUID)) {
                byte[] pressureBytes = characteristic.getValue();
                if (pressureBytes != null) {
                    int numFloats = pressureBytes.length / 4;
                    float[] pressures = new float[numFloats];
                    ByteBuffer.wrap(pressureBytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(pressures);
                    latestPressures = pressures;
                    updatePressure(pressures);
                    Log.d("Bluetooth", "Pressures updated: " + Arrays.toString(pressures) + " kPa");
                } else {
                    Log.w("Bluetooth", "Pressure byte array is null");
                }
            }
        }
    };

    private void updatePressure(float[] pressures) {
        String token = Helper.getToken(context);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("MTK-1", pressures[0]);
            jsonObject.put("MTK-2", pressures[1]);
            jsonObject.put("MTK-3", pressures[2]);
            jsonObject.put("MTK-4", pressures[3]);
            jsonObject.put("MTK-5", pressures[4]);
            jsonObject.put("D1", pressures[5]);
            jsonObject.put("Lateral", pressures[6]);
            jsonObject.put("Calcaneus", pressures[7]);
        } catch (Exception e) {
            Log.e("BluetoothService", "Failed to create JSON object for Pressure data: " + e.getMessage());
            return;
        }

        // update the user's data in the database
        apiService.postData(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("BluetoothService", "Pressure data updated successfully: " + response);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("BluetoothService", "Failed to update Pressure data: " + errorMessage);
            }
        });
    }

    private void updateTemperature(float[] temperatures) {
        String token = Helper.getToken(context);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("MTK-1", temperatures[0]);
            jsonObject.put("MTK-2", temperatures[1]);
            jsonObject.put("MTK-3", temperatures[2]);
            jsonObject.put("MTK-4", temperatures[3]);
            jsonObject.put("MTK-5", temperatures[4]);
            jsonObject.put("D1", temperatures[5]);
            jsonObject.put("Lateral", temperatures[6]);
            jsonObject.put("Calcaneus", temperatures[7]);
        } catch (Exception e) {
            Log.e("BluetoothService", "Failed to create JSON object for Temperature data: " + e.getMessage());
            return;
        }

        // update the user's data in the database
        apiService.postData(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("BluetoothService", "Temperature data updated successfully: " + response);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("BluetoothService", "Failed to update Temperature data: " + errorMessage);
            }
        });
    }

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
            Log.d("Bluetooth", "Enabling notifications for characteristic: " + characteristicUUID);
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

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e("BluetoothService", "BluetoothManager not initialized");
            return new ArrayList<>();
        }

        // Get connected devices for the GATT profile
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BluetoothService", "getConnectedDevices - Bluetooth scan permission not granted");
            ActivityCompat.requestPermissions((PairingActivity) context, new String[]{Manifest.permission.BLUETOOTH}, 1);
            return new ArrayList<>();
        }
        return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
    }


    public void setOnDeviceFoundListener(OnDeviceFoundListener listener) {
        this.onDeviceFoundListener = listener;
    }

    public float[] getLatestTemperature() {
        return latestTemperatures;
    }

    public float[] getLatestPressures() {
        return latestPressures;
    }
}
