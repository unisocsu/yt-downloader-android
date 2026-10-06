package com.example.youtubedownloader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

public class MainActivity extends Activity {
    private EditText urlInput;
    private Button downloadButton;
    private ProgressBar progressBar;
    private TextView statusText;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!DownloadService.ACTION_STATUS.equals(intent.getAction())) return;

            String message = intent.getStringExtra("message");
            int progress = intent.getIntExtra("progress", 0);

            statusText.setText(message);
            if (progress >= 0) {
                progressBar.setProgress(progress);
                progressBar.setVisibility(View.VISIBLE);
            }
            if (progress == 100 || progress < 0) {
                progressBar.setVisibility(View.GONE);
                downloadButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = (EditText) findViewById(R.id.url_input);
        downloadButton = (Button) findViewById(R.id.download_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusText = (TextView) findViewById(R.id.status_text);

        progressBar.setVisibility(View.GONE);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startDownload();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(statusReceiver, new IntentFilter(DownloadService.ACTION_STATUS));
    }

    @Override
    protected void onPause() {
        try { unregisterReceiver(statusReceiver); } catch (Exception ignored) {}
        super.onPause();
    }

    private void startDownload() {
        String url = urlInput.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.enter_url, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(R.string.download_starting);
        downloadButton.setEnabled(false);

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_URL, url);
        startService(intent);
    }
}
