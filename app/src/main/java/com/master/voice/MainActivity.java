package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        resultText = findViewById(R.id.result_text);

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Load model in background
        new Thread(() -> {
            try {
                File modelDir = new File(Environment.getExternalStorageDirectory(), "model");
                if (!modelDir.exists()) {
                    modelDir = new File(getExternalFilesDir(null), "model");
                }

                Model model = new Model(modelDir.getAbsolutePath());
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(MainActivity.this);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Model loaded from: " + modelDir.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Model load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();

        // Start listening when button is clicked
        btnStart.setOnClickListener(view -> {
            if (speechService != null) {
                speechService.startListening(MainActivity.this);
            }
        });

        // Initialize Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public void onPartialResult(String hypothesis) {
        resultText.setText(hypothesis);
    }

    @Override
    public void onResult(String hypothesis) {
        resultText.setText(hypothesis);
        tts.speak(hypothesis, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        resultText.setText(hypothesis);
    }

    @Override
    public void onError(Exception e) {
        resultText.setText("Error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        resultText.setText("Timeout!");
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
