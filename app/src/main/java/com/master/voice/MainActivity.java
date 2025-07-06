package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

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

        // Permission request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Load VOSK model in background thread
        new Thread(() -> {
            try {
                Model model = new Model(MainActivity.this.getExternalFilesDir(null) + "/model");
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(MainActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Button click listener
        btnStart.setOnClickListener(view -> {
            if (speechService != null) {
                speechService.startListening(this);
            }
        });

        // Text-to-Speech Init
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
