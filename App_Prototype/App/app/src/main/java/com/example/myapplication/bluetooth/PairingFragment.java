package com.example.myapplication.bluetooth;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import java.util.ArrayList;

public class PairingFragment extends Fragment implements BluetoothService.OnDeviceFoundListener {
    private ListView pairingListView;
    private final ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothService bluetoothService;
    private static final int PERMISSION_REQUEST_CODE = 2;

    public PairingFragment() {}

    public static PairingFragment newInstance() {
        return new PairingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothService = BluetoothService.getInstance(getContext());
        bluetoothService.setConnectionListener(new ConnectionListener() {
            @Override
            public void onDeviceConnected() {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Device connected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                });
            }

            @Override
            public void onDeviceDisconnected() {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Device disconnected", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onConnectionFailure() {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Connection failure", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pairing, container, false);
        pairingListView = view.findViewById(R.id.pairingListView);
        TextView textView5 = view.findViewById(R.id.textView5);
        Button rescanButton = view.findViewById(R.id.rescanButton);
        rescanButton.setOnClickListener(v -> {
            Log.d("PairingFragment", "Rescan button clicked");
            deviceList.clear();
            checkPermissionsAndStartScanning();
            updatePairingUI();
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        String greeting = "Hello, " + username.substring(0, 1).toUpperCase() + username.substring(1) + "!";
        textView5.setText(greeting);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pairingListView.setOnItemClickListener((parent, itemView, position, id) -> {
            String deviceInfo = (String) parent.getItemAtPosition(position);
            String[] parts = deviceInfo.split("\n");
            if (parts.length == 2) {
                BluetoothDevice device = bluetoothService.getRemoteDevice(parts[1]);
                if (device != null) {
                    bluetoothService.connectToDevice(device);
                }
            } else {
                Log.w("PairingFragment", "Invalid device info: " + deviceInfo);
            }
        });

        bluetoothService.setOnDeviceFoundListener(this);
        checkPermissionsAndStartScanning();
        updatePairingUI();

        return view;
    }

    private void checkPermissionsAndStartScanning() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            bluetoothService.startScanning();
        }
    }

    private void updatePairingUI() {
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);
        pairingListView.setAdapter(adapter);
    }

    @Override
    public void onDeviceFound(String deviceInfo) {
        getActivity().runOnUiThread(() -> {
            deviceList.add(deviceInfo);
            adapter.notifyDataSetChanged();
        });
    }
}