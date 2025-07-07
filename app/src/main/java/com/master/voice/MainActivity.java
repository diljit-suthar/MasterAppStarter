package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private TextView resultTextView;
    private SpeechService speechService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the resultTextView from XML
        resultTextView = findViewById(R.id.resultTextView);

        // Check for microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        } else {
            initModel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initModel();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void initModel() {
        new Thread(() -> {
            try {
                // Load the Vosk model
                final File modelDir = new File(getExternalFilesDir(null), "model"); // FIXED (final)
                Model model = new Model(modelDir.getAbsolutePath());
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this); // Start listening for speech

                // Display a success message
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Model loaded from: " + modelDir.getAbsolutePath(),
                        Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Failed to load model: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultTextView.setText(hypothesis));  // Show partial results
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> resultTextView.setText(hypothesis));  // Show final result
    }

    @Override
    public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> resultTextView.setText(hypothesis));  // Show final result
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this,
                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this,
                "Timeout", Toast.LENGTH_LONG).show());
    }
}
