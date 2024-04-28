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
    private ImageView imageViewGraph;
    private ImageView imageView2;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        apiService = ApiService.getInstance();

        ToggleButton toggleButton = view.findViewById(R.id.toggleButton);

        imageViewGraph = view.findViewById(R.id.imageViewGraph);
        imageView2 = view.findViewById(R.id.imageView2);


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
                // bring back image
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
                //requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());

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
                //requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), response, Toast.LENGTH_SHORT).show());

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
}