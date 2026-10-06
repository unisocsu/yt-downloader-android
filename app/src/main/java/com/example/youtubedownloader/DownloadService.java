package com.example.youtubedownloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;

public class DownloadService extends Service {
    public static final String EXTRA_URL = "url";

    private Handler handler;

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public int onStartCommand(final Intent intent, int flags, int startId) {
        final String url = intent != null ? intent.getStringExtra(EXTRA_URL) : null;

        new Thread(new Runnable() {
            @Override public void run() {
                // Downloader engine is intentionally isolated here.
                final String result = "URL התקבלה: " + url;
                handler.post(new Runnable() {
                    @Override public void run() {
                        sendStatus(result);
                        stopSelf();
                    }
                });
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void sendStatus(String message) {
        Intent broadcast = new Intent("com.example.youtubedownloader.STATUS");
        broadcast.putExtra("message", message);
        sendBroadcast(broadcast);
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
