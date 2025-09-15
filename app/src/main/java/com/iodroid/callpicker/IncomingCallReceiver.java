package com.iodroid.callpicker;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class IncomingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Toast.makeText(context, "Incoming Call from: " + incomingNumber, Toast.LENGTH_LONG).show();

            // Attempt to answer the call (works only in system apps)
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


            if (telecomManager != null) {
                // This answers the call
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();

                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telecomManager.acceptRingingCall();
                    if (audioManager != null) {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                }
            }
        }
    }
}
