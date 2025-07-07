package com.master.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private Button btnStart;
    private TextView resultText;
    private SpeechService speechService;
    private TextToSpeech tts;

    private static final int PERMISSIONS_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStart);
        resultText = findViewById(R.id.result_text);

        requestNecessaryPermissions();

        // Text-to-Speech Initialization
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Load VOSK model in background
        new Thread(this::loadModel).start();

        // Button to start listening
        btnStart.setOnClickListener(view -> {
            if (speechService != null) {
                speechService.startListening(this);
                Toast.makeText(this, "Started Listening", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Model not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadModel() {
        try {
            File modelDir;

            // 1. Try app's internal storage
            modelDir = new File(getFilesDir(), "model");

            // 2. If not found, try /storage/emulated/0/model
            if (!modelDir.exists() || modelDir.listFiles() == null || modelDir.listFiles().length == 0) {
                modelDir = new File(Environment.getExternalStorageDirectory(), "model");
            }

            if (!modelDir.exists()) {
                runOnUiThread(() -> resultText.setText("Model not found in any location."));
                return;
            }

            Model model = new Model(modelDir.getAbsolutePath());
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);

            runOnUiThread(() -> {
                btnStart.setEnabled(true);
                Toast.makeText(this, "Model loaded from: " + modelDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
            });

        } catch (IOException e) {
            runOnUiThread(() -> resultText.setText("Model loading failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void requestNecessaryPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {

            // Request MANAGE_EXTERNAL_STORAGE for Android 11+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }

            // Request RECORD_AUDIO and other needed permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        resultText.setText("Partial: " + hypothesis);
    }

    @Override
    public void onResult(String hypothesis) {
        resultText.setText("Result: " + hypothesis);
        if (tts != null) {
            tts.speak(hypothesis, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        resultText.setText("Final: " + hypothesis);
    }

    @Override
    public void onError(Exception e) {
        resultText.setText("Error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        resultText.setText("Timeout reached.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
        }
        if (tts != null) {
            tts.shutdown();
        }
    }
}
