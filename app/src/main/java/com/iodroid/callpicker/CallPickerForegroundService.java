package com.iodroid.callpicker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CallPickerForegroundService extends Service {
    private static final String CHANNEL_ID = "foreground_timer_channel";
    private int timeInSeconds = 0;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private boolean isPaused = false;

    private static final int TIME_INTERVAL_MS = 5 * 60 * 1000; // 300000ms = 5 minutes
    private static final int TIME_INCREMENT_SECONDS = 5 * 60; // 300 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    timeInSeconds++;
                    updateNotification(timeInSeconds);
                }
                handler.postDelayed(this, 1000);
            }
        };

        createNotificationChannel();
        startCountdown();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Foreground Timer Service";
            String description = "Channel for timer service";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startCountdown() {
        handler.post(updateTimeRunnable);
    }

    private void updateNotification(int timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Picker is running")
                .setContentText("Elapsed time: "+ timeFormatted)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_MUTABLE));

        startForeground(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if ("PAUSE_TIMER".equals(intent.getAction())) {
                isPaused = !isPaused;
            } else if ("STOP_TIMER".equals(intent.getAction())) {
                stopSelf();
                handler.removeCallbacks(updateTimeRunnable); // Stop updating the timer
            }
        }
        if (!isPaused) {
            handler.post(updateTimeRunnable);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
