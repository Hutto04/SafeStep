---
sidebar_position: 3
---

# Android App

This section will deep dive into the Android application of our project.

## What you will need:

We will not be going over installing any software as it is a straightforward process.

- Android Studio
- Android Emulator 
- Physical Android device (for Bluetooth testing - emulator does not support Bluetooth)
- Java SDK
- Python installed on your system

## Setting up the Android App

1. Head to our [GitHub repository](https://github.com/SafeStepCSU/SafeStep) and clone or download the project to your local machine.
    - Ensure you have the `dev` branch selected.
    - Head to the directory `SafeStep/App_Prototype` and open the project in Android Studio like so:
        1. Open Android Studio and click `File` -> `Open` -> 
        2. Navigate to the `SafeStep/App_Prototype` directory and select the `App` folder.
        3. Click `OK` and your project should open in Android Studio.

2. Set up [chaquopy](https://chaquo.com/chaquopy/doc/current/android.html) in the project:
    - [Chaquopy](https://chaquo.com/chaquopy/doc/current/android.html) is a plugin that allows you to run Python code in your Android application.
    - We've done all the setup prep, all you need to go is navigate to your Gradle Scripts and open the `build.gradle` (Module: app) file.
    - Look for `buildPython` under `defaultConfig` and **change the path to the location of your Python executable.**
      - This is important for the Python code to run on your Android device.
    - Now do a Gradle build by clicking the `Sync` button that appears at the top right corner of Android Studio.


3. Upon a successful build, you should now be able to run the application on an emulator or physical device.
    - To run the application on an emulator, click the `Run` button in the top right corner of Android Studio.
    - To run the application on a physical device, turn your device on `developer mode` and connect it to your computer via USB. Then click the `Run` button in the top right corner of Android Studio.
      - For more information on how to enable developer mode on your device, please refer to the [Android Developer Documentation](https://developer.android.com/studio/debug/dev-options).

## Current Features

- Login and Registration
- Bluetooth connectivity
- Profile page
  - Users can view and edit their profile information
- Pairing page
  - Users can pair their device with a device (the Pico)
- Debugging page
  - This was really just for testing purposes and can be removed in the future
    - You can see current connected device
    - You can get the latest temperature and pressure readings at will
    - You can send MOCK GET requests to the server
    - You can send MOCK POST requests to the server

## Important Classes

- **ApiService**
  - This class is responsible for making API calls to the server.

- **BluetoothService**
   - This class is responsible for handling Bluetooth connectivity.

- **Helper**
  - This class contains helper methods that are used throughout the application:
    - `URL` - the base URL for the web server in which the API calls are made.
      - Change this as needed. It is currently set to our server hosted on Render. (https://safestep.onrender.com/)
    - `getToken` method which is used to get the token from the shared preferences.
    - Should be refactored in the future to include more helper methods. A lot of things could of been added here.



