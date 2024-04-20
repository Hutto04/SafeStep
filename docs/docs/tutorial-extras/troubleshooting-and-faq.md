---
sidebar_position: 1
---

# Troubleshooting

This section will cover common issues and questions that may arise when working with SafeStep.

## General Troubleshooting

1. ### [Device stuck on `launching` state](https://stackoverflow.com/questions/51101178/android-studio-device-list-stuck-on-loading?page=1&tab=scoredesc#tab-top)
   - Try performing a cold boot. Go to `Device Manager` and right-click on the device, then click `Cold boot`.

2. ### [Error 133 on Bluetooth Connection](https://stackoverflow.com/questions/47472916/ble-gatt-onconnectionstatechange-failed-status-133-and-257)
   - Specify the transport parameter in your GATT connection request. Use the optional fourth parameter to indicate that it's a BLE device: device.connectGatt(context, false, callback, **2**);.

3. ### USB Debugging Not Working
   - If you are on a Mac, use a USB-C to USB-C cable for connecting your device to a Mac. This can help establish a more stable connection suitable for debugging.

4. ### Wireless Debugging Issues
   - Ensure that both the development machine and the Android device are on the same WiFi network. Check your network settings and security configurations that might block the connection.

## I don't see my issue here

If you are facing an issue that is not covered here, please create an issue on our [GitHub repository](https://github.com/SafeStepCSU/SafeStep/issues).