package com.example.youtubedownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

public class DownloadService extends Service {
    public static final String EXTRA_URL = "url";
    public static final String ACTION_STATUS = "com.example.youtubedownloader.STATUS";

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        final String url = intent != null ? intent.getStringExtra(EXTRA_URL) : null;

        new Thread(new Runnable() {
            @Override public void run() {
                downloadWithYtDlp(url);
                stopSelf(startId);
            }
        }, "yt-dlp-download").start();

        return START_NOT_STICKY;
    }

    private void downloadWithYtDlp(String url) {
        try {
            if (url == null || url.length() == 0) {
                sendStatus("שגיאה: כתובת ריקה", -1);
                return;
            }

            sendStatus("מאתחל מנוע yt-dlp...", 0);
            YoutubeDL.getInstance().init(getApplication());

            File downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File outputDir = new File(downloads, "yt-downloader");
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                sendStatus("שגיאה: לא ניתן ליצור תיקיית הורדות", -1);
                return;
            }

            YoutubeDLRequest request = new YoutubeDLRequest(url);

            // Prefer a single MP4 stream when available. This avoids requiring
            // a separate FFmpeg package for the basic API19 build.
            request.addOption("-f", "best[ext=mp4]/best");
            request.addOption("--no-playlist");
            request.addOption("--no-mtime");
            request.addOption("-o",
                    new File(outputDir, "%(title)s.%(ext)s").getAbsolutePath());

            sendStatus("מאתר וידאו...", 1);

            YoutubeDL.getInstance().execute(request, new DownloadProgressCallback() {
                @Override
                public void onProgressUpdate(float progress, long etaInSeconds) {
                    int p = (int) Math.max(0, Math.min(100, progress));
                    String message = "מוריד... " + p + "%";
                    if (etaInSeconds >= 0) {
                        message += " (נותרו " + etaInSeconds + " שניות)";
                    }
                    sendStatus(message, p);
                }
            });

            sendStatus("ההורדה הושלמה: " + outputDir.getAbsolutePath(), 100);

        } catch (YoutubeDLException e) {
            String message = e.getMessage();
            if (message == null || message.length() == 0) message = e.toString();
            sendStatus("yt-dlp: " + message, -1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendStatus("ההורדה הופסקה", -1);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.length() == 0) message = e.toString();
            sendStatus("שגיאה: " + message, -1);
        }
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
