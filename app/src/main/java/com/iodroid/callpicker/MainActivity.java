package com.iodroid.callpicker;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BLManager.BLEListener {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private BLManager BLManager;
    private TextView statusText;
    private TextView messageText;
    private Button scanButton;
    private Button srtButton;
    private Button endButton;
    private ListView deviceList;

    private ArrayAdapter<String> deviceAdapter;
    private List<String> deviceNames;
    private Map<String, BluetoothDevice> deviceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        statusText = findViewById(R.id.statusText);
        messageText = findViewById(R.id.messageText);
        scanButton = findViewById(R.id.scanButton);
        srtButton = findViewById(R.id.srtButton);
        endButton = findViewById(R.id.endButton);
        deviceList = findViewById(R.id.deviceList);

        // Initialize device list
        deviceNames = new ArrayList<>();
        deviceMap = new HashMap<>();
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        deviceList.setAdapter(deviceAdapter);

        // Initialize BLE manager
        BLManager = new BLManager(this, this);

        // Check permissions
        checkPermissions();

        // Set listeners
        scanButton.setOnClickListener(v -> startScanning());
        srtButton.setOnClickListener(v -> sendCommand("SRT"));
        endButton.setOnClickListener(v -> sendCommand("END"));

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = deviceNames.get(position);
            BluetoothDevice device = deviceMap.get(deviceName);
            if (device != null) {
                connectToDevice(device);
            }
        });

        // Initially disable command buttons
        setCommandButtonsEnabled(false);
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            permissions.add(Manifest.permission.BLUETOOTH);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

//        if (!permissionsToRequest.isEmpty()) {
//            ActivityCompat.requestPermissions(this,
//                    permissionsToRequest.toArray(new String[0]),
//                    PERMISSION_REQUEST_CODE);
//        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, 3);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 4);
    }

    private void startScanning() {
        deviceNames.clear();
        deviceMap.clear();
        deviceAdapter.notifyDataSetChanged();
        statusText.setText("Scanning...");
        BLManager.startScan();

        // Disable scan button during scanning
        scanButton.setEnabled(false);
        new android.os.Handler().postDelayed(() -> {
            scanButton.setEnabled(true);
            if (statusText.getText().toString().equals("Scanning...")) {
                statusText.setText("Scan complete");
            }
        }, 10000);
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        statusText.setText("Connecting to " + device.getName() + "...");
        BLManager.connectToDevice(device);
    }

    private void sendCommand(String command) {
        if (BLManager.sendMessage(command)) {
            Toast.makeText(this, "Sent: " + command, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCommandButtonsEnabled(boolean enabled) {
        srtButton.setEnabled(enabled);
        endButton.setEnabled(enabled);
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String name = device.getName();
        if (name == null || name.isEmpty()) {
            name = device.getAddress();
        }

        if (!deviceMap.containsKey(name)) {
            deviceNames.add(name);
            deviceMap.put(name, device);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnected() {
        statusText.setText("Connected");
        setCommandButtonsEnabled(true);
    }

    @Override
    public void onDisconnected() {
        statusText.setText("Disconnected");
        setCommandButtonsEnabled(false);
    }

    @Override
    public void onMessageReceived(String message) {
        messageText.setText("Received: " + message);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BLManager != null) {
            BLManager.disconnect();
        }
    }
}