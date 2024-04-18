package com.example.myapplication.bluetooth;

public interface ConnectionListener {
    void onDeviceConnected();
    void onDeviceDisconnected();
    void onConnectionFailure();
}
