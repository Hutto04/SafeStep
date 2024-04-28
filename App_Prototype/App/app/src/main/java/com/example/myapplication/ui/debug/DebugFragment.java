package com.example.myapplication.ui.debug;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapplication.Helper;
import com.example.myapplication.R;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.bluetooth.BluetoothService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DebugFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DebugFragment extends Fragment {
    private ApiService apiService;
    private BluetoothService bluetoothService;

    public DebugFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static DebugFragment newInstance() {
        DebugFragment fragment = new DebugFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = apiService.getInstance();
        bluetoothService = BluetoothService.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debug, container, false);


        Button getButton = view.findViewById(R.id.getButton);
        Button postButton = view.findViewById(R.id.postButton);
        Button pairedButton = view.findViewById(R.id.pairedButton);
        Button dataButton = view.findViewById(R.id.bleScanButton);

        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);

        ImageView imageViewGraph = view.findViewById(R.id.imageViewGraph);
        ImageView imageView2 = view.findViewById(R.id.imageView2);

        getButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "GET Button clicked");
            testProtectedGetRequest();
        });

        postButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "POST Button clicked");
            testProtectedPostRequest();
        });

        pairedButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "Paired Button clicked");
            Log.d("Debug", "Connected Devices: " + bluetoothService.getConnectedDevices());
        });

        dataButton.setOnClickListener(v -> {
            float[] pressures = bluetoothService.getLatestPressures();
            float[] temps = bluetoothService.getLatestTemperatures();
            Log.d("Debug", "Pressures: " + Arrays.toString(pressures));
            Log.d("Debug", "Temperatures: " + Arrays.toString(temps));
        });


        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(requireContext()));
        }

        Python py = Python.getInstance();
        PyObject myModule = py.getModule("test");
        PyObject myFnCallVale = myModule.get("simple_sort");
        int[] numList = {1, 3, 2, 5, 9};
        System.out.println("The Function Call's Return Value: " + myFnCallVale.call(numList).toString());

        // Get the chart when the toggle button is clicked
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("HomeFragment", "Toggle Button clicked");
            if (isChecked) {
                // heatmap
                getHeatMap();
                imageView2.setVisibility(View.GONE);
                Log.d("HomeFragment", "Toggle Button is ON");
            } else {
                getChart();
                imageView2.setVisibility(View.VISIBLE);
                Log.d("HomeFragment", "Toggle Button is OFF");
            }
        });

        getChart(); // Get the chart when the user first opens the app
        return view;
    }


    private void getHeatMap() {
        // Get data from DB
        apiService.getUserData(Helper.getToken(requireContext()), new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("HomeFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());

                try {
                    // Parse the JSON response
                    JSONArray jsonArray = new JSONArray(response);

                    // Get the first object in the array
                    if (jsonArray.length() == 0) {
                        // TODO: tell the user that there is no data available
                        Log.d("HomeFragment", "No pressure data available");
                        return;
                    }

                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    JSONObject pressureData = jsonObject.getJSONObject("pressure_data");

                    Log.d("HomeFragment", "JSON Object: " + jsonObject);
                    Log.d("HomeFragment", "Pressure Data: " + pressureData);

                    // Convert JSONArrays to Python lists
                    // Extract pressure data for each point
                    double mtk_1 = pressureData.getDouble("MTK-1");
                    double mtk_2 = pressureData.getDouble("MTK-2");
                    double mtk_3 = pressureData.getDouble("MTK-3");
                    double mtk_4 = pressureData.getDouble("MTK-4");
                    double mtk_5 = pressureData.getDouble("MTK-5");
                    double d1 = pressureData.getDouble("D1");
                    double lateral = pressureData.getDouble("Lateral");
                    double calcaneus = pressureData.getDouble("Calcaneus");

                    Log.d("HomeFragment", "MTK-1: " + mtk_1);
                    Log.d("HomeFragment", "MTK-2: " + mtk_2);
                    Log.d("HomeFragment", "MTK-3: " + mtk_3);
                    Log.d("HomeFragment", "MTK-4: " + mtk_4);
                    Log.d("HomeFragment", "MTK-5: " + mtk_5);
                    Log.d("HomeFragment", "D1: " + d1);
                    Log.d("HomeFragment", "Lateral: " + lateral);
                    Log.d("HomeFragment", "Calcaneus: " + calcaneus);

                    // Generate the graph using Chaquopy
                    Python py = Python.getInstance();
                    PyObject myModule = py.getModule("heatmap");
                    PyObject heatmapBytes = myModule.callAttr("generate_heatmap",
                            mtk_1, mtk_2, mtk_3, mtk_4, mtk_5, d1, lateral, calcaneus,
                            mtk_1, mtk_2, mtk_3, mtk_4, mtk_5, d1, lateral, calcaneus);

                    // Convert the bytes to a byte array
                    byte[] heatmapData = heatmapBytes.toJava(byte[].class);

                    // Create a Bitmap from the byte array
                    Bitmap bitmap = BitmapFactory.decodeByteArray(heatmapData, 0, heatmapData.length);

                    // TODO: Cache the bitmap to avoid regenerating it every time, only regenerate when new data is available or on a schedule (like every 30 minutes)
                    // Set the bitmap to the ImageView on the main thread
                    ImageView imageViewGraph = getView().findViewById(R.id.imageViewGraph);
                    requireActivity().runOnUiThread(() -> imageViewGraph.setImageBitmap(bitmap));
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error parsing JSON", e);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("HomeFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void getChart() {
        // Get data from DB
        apiService.getUserData(Helper.getToken(requireContext()), new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("HomeFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());

                try {
                    // Parse the JSON response
                    JSONArray jsonArray = new JSONArray(response);

                    // Get the first object in the array
                    if (jsonArray.length() == 0) {
                        Log.d("HomeFragment", "No pressure data available");
                        return;
                    }

                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    JSONObject pressureData = jsonObject.getJSONObject("pressure_data");

                    Log.d("HomeFragment", "JSON Object: " + jsonObject);
                    Log.d("HomeFragment", "Pressure Data: " + pressureData);

                    // Initialize lists to hold the keys and values
                    List<String> keysList = new ArrayList<>();
                    List<Integer> valuesList = new ArrayList<>();

                    // Iterate over all keys in the pressureData object
                    Iterator<String> keys = pressureData.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        int value = pressureData.getInt(key);
                        keysList.add(key);
                        valuesList.add(value);
                    }

                    // Convert the lists to arrays if necessary
                    String[] x = keysList.toArray(new String[0]);
                    int[] y = valuesList.stream().mapToInt(i->i).toArray();

                    Log.d("HomeFragment", "X: " + Arrays.toString(x));
                    Log.d("HomeFragment", "Y: " + Arrays.toString(y));

                    // Generate the graph using Chaquopy
                    Python py = Python.getInstance();
                    PyObject myModule = py.getModule("test2");
                    PyObject graphBytes = myModule.get("generate_graph").call(x, y);

                    // Convert the bytes to a byte array
                    byte[] graphData = graphBytes.toJava(byte[].class);

                    // Create a Bitmap from the byte array
                    Bitmap bitmap = BitmapFactory.decodeByteArray(graphData, 0, graphData.length);

                    // TODO: Cache the bitmap to avoid regenerating it every time, only regenerate when new data is available or on a schedule (like every 30 minutes)
                    // Set the bitmap to the ImageView on the main thread
                    ImageView imageViewGraph = getView().findViewById(R.id.imageViewGraph);
                    requireActivity().runOnUiThread(() -> imageViewGraph.setImageBitmap(bitmap));
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error parsing JSON", e);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("HomeFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void testProtectedGetRequest() {
        String token = Helper.getToken(requireContext());
        Log.d("HomeFragment", "Token: " + token);

        apiService.getUserData(token, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("HomeFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("HomeFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void testProtectedPostRequest() {
        String token = Helper.getToken(requireContext());
        JSONObject jsonObject = new JSONObject();
        // Add data to the JSON object
        // TODO: Get data from sensors and add it to the JSON object
        try {
            jsonObject.put("sensor1", 75);
            jsonObject.put("temperature", 25);
        } catch (Exception e) {
            e.printStackTrace();
        }

        apiService.postData(token, jsonObject, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("HomeFragment", "Response: " + response);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("HomeFragment", "Error: " + errorMessage);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}