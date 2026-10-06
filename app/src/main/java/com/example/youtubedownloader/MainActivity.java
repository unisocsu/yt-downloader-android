package com.example.youtubedownloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText urlInput;
    private Button downloadButton;
    private ProgressBar progressBar;
    private TextView statusText;

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

        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(R.string.download_starting);
        downloadButton.setEnabled(false);

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_URL, url);
        startService(intent);
    }
}
