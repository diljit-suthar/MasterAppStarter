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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = findViewById(R.id.txtResult);
        Button btnStart = findViewById(R.id.btnStart);

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        btnStart.setOnClickListener(view -> {
            try {
                Model model = new Model(getApplicationContext().getExternalFilesDir(null) + "/model");
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                txtResult.setText("Error initializing model: " + e.getMessage());
            }
        });
    }

    @Override
    public void onResult(String hypothesis) {
        txtResult.setText(hypothesis);
    }

    @Override
    public void onPartialResult(String hypothesis) {}

    @Override
    public void onFinalResult(String hypothesis) {
        txtResult.setText(hypothesis);
    }

    @Override
    public void onError(Exception e) {
        txtResult.setText("Error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        txtResult.setText("Timeout");
    }
}