package com.iodroid.callpicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class IncomingCallReceiver extends BroadcastReceiver {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("CallState/state");

//    private String lastIncomingNumber = null;
//    private long callStartTime = 0;
//    private long callEndTime = 0;

    String lastState = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (state != null && !state.equals(lastState)) {
            lastState = state;
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (incomingNumber != null) {
//                    lastIncomingNumber = incomingNumber;
                    Toast.makeText(context, "Incoming Call from: " + incomingNumber, Toast.LENGTH_LONG).show();
                }
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                if (telecomManager != null) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                telecomManager.acceptRingingCall();
                                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                audioManager.setMode(AudioManager.MODE_IN_CALL);
                                audioManager.setSpeakerphoneOn(true);
                            }
                        }
                    }, 5000);
                }
            }

            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                Toast.makeText(context, "Call connected", Toast.LENGTH_LONG).show();
                myRef.setValue("call connected");
//                callStartTime = System.currentTimeMillis();
            }

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                Toast.makeText(context, "Call disconnected", Toast.LENGTH_LONG).show();
                myRef.setValue("idle");
//                callEndTime = System.currentTimeMillis();
//                long duration = (callEndTime - callStartTime) / 1000; // Duration in seconds

//                JSONObject callDetails = new JSONObject();
//                try {
//                    callDetails.put("number", lastIncomingNumber);
//                    callDetails.put("duration", duration);
//                    callDetails.put("start_time", callStartTime);
//                    callDetails.put("end_time", callEndTime);
//                    jsonData = callDetails.toString();
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
            }
        }
    }
}
