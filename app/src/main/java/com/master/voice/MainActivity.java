package com.master.voice;

import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Recognizer;
import org.vosk.Model;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechService speechService;
    private TextView txtResult;
    private Model model;
    private Recognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = findViewById(R.id.txtResult);
        Button btnStart = findViewById(R.id.btnStart);

        // Check and request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        // Initialize Vosk model only once
        try {
            model = new Model(getApplicationContext().getExternalFilesDir(null) + "/model");
            recognizer = new Recognizer(model, 16000.0f);
        } catch (IOException e) {
            txtResult.setText("Model init error: " + e.getMessage());
        }

        // Start listening
        btnStart.setOnClickListener(view -> {
            if (speechService == null) {
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);
            }
        });
    }

    @Override
    public void onResult(String hypothesis) {
        // Safe single result handler
        txtResult.setText("Result: " + hypothesis);
    }

    @Override
    public void onPartialResult(String hypothesis) {
        // Optional
    }

    @Override
    public void onFinalResult(String hypothesis) {
        // Avoid duplicating result output
    }

    @Override
    public void onError(Exception e) {
        txtResult.setText("Error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        txtResult.setText("Timeout");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
    }
}
