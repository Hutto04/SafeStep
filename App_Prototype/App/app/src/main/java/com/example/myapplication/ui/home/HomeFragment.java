package com.example.myapplication.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private ApiService apiService;
    private BluetoothService bluetoothService;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        apiService = ApiService.getInstance();
        bluetoothService = new BluetoothService(requireContext());

        Button getButton = view.findViewById(R.id.getButton);
        Button postButton = view.findViewById(R.id.postButton);
        Button bleScanButton = view.findViewById(R.id.bleScanButton);
        Button pairedButton = view.findViewById(R.id.pairedButton);
        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);

        ImageView imageViewGraph = view.findViewById(R.id.imageViewGraph);

        getButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "GET Button clicked");
            testProtectedGetRequest();
        });

        postButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "POST Button clicked");
            testProtectedPostRequest();
        });

        bleScanButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "Scan Button clicked");
            bluetoothService.scanLeDevice();
        });

        pairedButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "Paired Button clicked");
            bluetoothService.getBluetoothDevices();
        });

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("HomeFragment", "Toggle Button clicked");
            if (isChecked) {
                Log.d("HomeFragment", "Toggle Button is ON");
            } else {
                Log.d("HomeFragment", "Toggle Button is OFF");
            }
        });

        // Start Python -
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(requireContext()));
        }

        // chaquopy - Python SDK so that we can call Python functions within java
        Python py = Python.getInstance();
        PyObject myModule = py.getModule("test");
        PyObject myFnCallVale = myModule.get("simple_sort");
        int[] numList = {1, 3, 2, 5, 9};
        System.out.println("The Function Call's Return Value: " + myFnCallVale.call(numList).toString());

        // Get the chart
        getChart();
        return view;
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