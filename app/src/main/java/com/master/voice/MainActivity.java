package com.master.voice;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextView resultText;
    private SpeechService speechService;
    private TextToSpeech tts;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.result_text);

        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModelAndRecognizer();
        }

        // Text to Speech setup
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });
    }

    private void initModelAndRecognizer() {
        new Thread(() -> {
            try {
                model = new Model(getAssets(), "model");
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Model loading failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModelAndRecognizer();
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText(hypothesis));
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> {
            resultText.setText(hypothesis);
            handleCommand(hypothesis);
        });
    }

    private void handleCommand(String command) {
        command = command.toLowerCase();
        if (command.contains("otg on")) {
            speak("Turning on OTG");
            openOTGSettings();
        } else if (command.contains("call my father")) {
            speak("Calling father");
            callFather();
        }
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void openOTGSettings() {
        Intent intent = new Intent();
        intent.setAction("android.settings.HARDWARE_SETTINGS");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "OTG settings not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void callFather() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(android.net.Uri.parse("tel:1234567890")); // Replace with actual number
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 2);
        }
    }

    @Override public void onFinalResult(String hypothesis) {}
    @Override public void onError(Exception e) {}
    @Override public void onTimeout() {}

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
