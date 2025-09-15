package com.iodroid.callpicker;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_CALL_PHONE = 2;
    private static final int REQUEST_ANSWER_PHONE = 3;

    Button answerPhoneButton;
    TextView answerPhonePermissionText;
    Button phoneStateButton;
    TextView phoneStatePermissionText;
    Button callPhoneButton;
    TextView callPhonePermissionText;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        answerPhoneButton = findViewById(R.id.answerPhoneButton);
        phoneStateButton = findViewById(R.id.phoneStateButton);
        callPhoneButton = findViewById(R.id.callPhoneButton);

        answerPhonePermissionText = findViewById(R.id.answerPhonePermissionText);
        phoneStatePermissionText = findViewById(R.id.phoneStatePermissionText);
        callPhonePermissionText = findViewById(R.id.callPhonePermissionText);

        answerPhonePermission();
        phoneStatePermission();
        callPhonePermission();

        answerPhoneButton.setOnClickListener(v -> {
            answerPhonePermission();
        });

        phoneStateButton.setOnClickListener(v -> {
            phoneStatePermission();
        });

        callPhoneButton.setOnClickListener(v -> {
            callPhonePermission();
        });

        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        IncomingCallReceiver receiver = new IncomingCallReceiver();
        registerReceiver(receiver, filter);

        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 2);
        }

        Intent intent = new Intent(this, CallPickerForegroundService.class);
        startForegroundService(intent);
    }

    private void startFgServiceSafely() {
        // Android 13+ needs POST_NOTIFICATIONS at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        42
                );
                return; // wait for user response then call again
            }
        }

        Intent svc = new Intent(this, CallPickerForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, svc);
        } else {
            startService(svc);
        }
    }

    // Optionally, stop it:
    private void stopFgService() {
        stopService(new Intent(this, CallPickerForegroundService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 42) {
            startFgServiceSafely();
        }
    }

    public void answerPhonePermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS}, REQUEST_ANSWER_PHONE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                answerPhonePermissionText.setText("Answer Phone Permission Granted!");
                answerPhonePermissionText.setTextColor(Color.rgb(0, 255, 0));
            } else{
                answerPhonePermissionText.setText("Answer Phone Permission Not Granted!");
                answerPhonePermissionText.setTextColor(Color.rgb(255, 0, 0));
            }
        } else {
            // Permission has already been granted
            answerPhonePermissionText.setText("Answer Phone Permission Granted!");
            answerPhonePermissionText.setTextColor(Color.rgb(0, 255, 0));
        }
    }

    public void phoneStatePermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneStatePermissionText.setText("Phone State Permission Granted!");
                phoneStatePermissionText.setTextColor(Color.rgb(0, 255, 0));
            } else{
                phoneStatePermissionText.setText("Phone State Permission Not Granted!");
                phoneStatePermissionText.setTextColor(Color.rgb(255, 0, 0));
            }
        } else {
            // Permission has already been granted
            phoneStatePermissionText.setText("Phone State Permission Granted!");
            phoneStatePermissionText.setTextColor(Color.rgb(0, 255, 0));
        }
    }

    public void callPhonePermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                callPhonePermissionText.setText("Call Phone Permission Granted!");
                callPhonePermissionText.setTextColor(Color.rgb(0, 255, 0));
            } else{
                callPhonePermissionText.setText("Call Phone Permission Not Granted!");
                callPhonePermissionText.setTextColor(Color.rgb(255, 0, 0));
            }
        } else {
            // Permission has already been granted
            callPhonePermissionText.setText("Call Phone Permission Granted!");
            callPhonePermissionText.setTextColor(Color.rgb(0, 255, 0));
        }
    }
}

