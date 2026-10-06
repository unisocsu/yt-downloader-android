package com.example.youtubedownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class DownloadService extends Service {
    public static final String EXTRA_URL = "url";
    public static final String ACTION_STATUS = "com.example.youtubedownloader.STATUS";

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final String url = intent != null ? intent.getStringExtra(EXTRA_URL) : null;

        new Thread(new Runnable() {
            @Override public void run() {
                download(url);
                stopSelf(startId);
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void download(String sourceUrl) {
        HttpURLConnection connection = null;
        BufferedInputStream input = null;
        FileOutputStream output = null;

        try {
            if (sourceUrl == null || sourceUrl.length() == 0) {
                sendStatus("שגיאה: כתובת ריקה", -1);
                return;
            }

            sendStatus("מתחבר...", 0);

            URL url = new URL(sourceUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                sendStatus("שגיאת HTTP: " + code, -1);
                return;
            }

            String name = getFileName(connection, url);
            File downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!downloads.exists() && !downloads.mkdirs()) {
                sendStatus("לא ניתן ליצור תיקיית הורדות", -1);
                return;
            }

            File target = new File(downloads, name);
            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(target);

            long total = connection.getContentLength();
            long done = 0;
            byte[] buffer = new byte[16 * 1024];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                done += count;

                int progress = total > 0 ? (int) Math.min(100, (done * 100L) / total) : 0;
                sendStatus("מוריד... " + progress + "%", progress);
            }

            output.flush();
            sendStatus("ההורדה הושלמה: " + target.getAbsolutePath(), 100);

        } catch (Exception e) {
            sendStatus("שגיאה: " + e.getMessage(), -1);
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) {}
            try { if (output != null) output.close(); } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    private String getFileName(HttpURLConnection connection, URL url) {
        String disposition = connection.getHeaderField("Content-Disposition");
        if (disposition != null) {
            int p = disposition.indexOf("filename=");
            if (p >= 0) {
                String value = disposition.substring(p + 9).trim();
                if (value.startsWith(""") && value.endsWith(""")) {
                    value = value.substring(1, value.length() - 1);
                }
                try { value = URLDecoder.decode(value, "UTF-8"); } catch (Exception ignored) {}
                if (value.length() > 0) return safeName(value);
            }
        }

        String path = url.getPath();
        String value = path != null ? path.substring(path.lastIndexOf('/') + 1) : "";
        if (value.length() == 0) value = "download_" + System.currentTimeMillis() + ".bin";
        return safeName(value);
    }

    private String safeName(String name) {
        return name.replaceAll("[\\/:*?"<>|]", "_");
    }

    private void sendStatus(String message, int progress) {
        Intent broadcast = new Intent(ACTION_STATUS);
        broadcast.setPackage(getPackageName());
        broadcast.putExtra("message", message);
        broadcast.putExtra("progress", progress);
        sendBroadcast(broadcast);
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
