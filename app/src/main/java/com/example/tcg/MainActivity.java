package com.example.tcg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;

    private Camera camera;
    private TextView textView;
    private TextToSpeech textToSpeech;

    private final Handler cameraStopHandler = new Handler();
    private static final int CAMERA_STOP_DELAY = 5000;

    private static final int PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("com.google.android.gms.actions.SEARCH_ACTION")) {}//optional
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(300);
        surfaceView = findViewById(R.id.camera);
        textView = findViewById(R.id.text);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int language = textToSpeech.setLanguage(Locale.ENGLISH);
                    String welcomeMessage = "Point the Camera to Scan"; // Your welcome message
                    float speechRate = 0.7f; // Adjust the value as needed
                    textToSpeech.setSpeechRate(speechRate);
                    textToSpeech.speak(welcomeMessage, TextToSpeech.QUEUE_FLUSH, null, null);

                }
            }
        });

        startCameraSource();
    }


    private void restartApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static final int TIME_THRESHOLD = 1000; // Time threshold in milliseconds
    private int volumeDownCount = 0;

    private int volumeUpCount = 0;
    private long lastVolumeDownTime = 0;
    private long lastVolumeUpTime = 0;



    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            long currentTime = System.currentTimeMillis();

            // Check if it's within the time threshold and increment the count
            if (currentTime - lastVolumeUpTime < TIME_THRESHOLD) {
                volumeUpCount++;
            } else {
                volumeUpCount = 1; // Reset count if time threshold exceeded
            }
            lastVolumeUpTime = currentTime;

            // Check if volume down key is pressed twice within the threshold
            if (volumeUpCount == 2) {
                String ReMessage = "Capturing the Image Again "; // Your welcome message
                textToSpeech.speak(ReMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                // Call your function here
                restartApplication();
                // Reset the count
                volumeUpCount = 0;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            long currentTime = System.currentTimeMillis();

            // Check if it's within the time threshold and increment the count
            if (currentTime - lastVolumeDownTime < TIME_THRESHOLD) {
                volumeDownCount++;
            } else {
                volumeDownCount = 1; // Reset count if time threshold exceeded
            }

            lastVolumeDownTime = currentTime;

            if (volumeDownCount==1){
                tts();
            }

            // Check if volume down key is pressed twice within the threshold
            if (volumeDownCount == 2) {
                // Call your function here
                closeApplication();
                // Reset the count
                volumeDownCount = 0;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            About();
        }

        return super.onKeyUp(keyCode, event);
    }
    private void About() {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }


    public void closeApplication() {
        String ExitMessage = "Thanks for using OptoTalk app! closing "; // Your welcome message
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(300);
        textToSpeech.speak(ExitMessage, TextToSpeech.QUEUE_FLUSH, null, null);
        Toast.makeText(getApplicationContext(), "Thanks for using OptoTalk app! closing ", Toast.LENGTH_SHORT).show();

        finishAffinity(); // This will finish the current activity and all parent activities
        // or
        // finish(); // This will finish only the current activity
    }
    private void startCameraSource() {

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w("Tag", "Dependencies not loaded yet");
        } else {
            CameraSource cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true).setRequestedFps(2.0f).build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION);
                            return;
                        }
                        // Start the camera source here
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    cameraStopHandler.removeCallbacksAndMessages(null); // Remove any existing callbacks
                    cameraStopHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();
                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(300);
                            String DoneMessage = "Image SuccessFully Captured "; // Your welcome message
                            // Inside an activity or fragment
                            Toast.makeText(getApplicationContext(), "Image SuccessFully Captured", Toast.LENGTH_SHORT).show();

                            textToSpeech.speak(DoneMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                            cameraSource.release();
                        }

                    }, CAMERA_STOP_DELAY);

                    //Release source for cameraSource
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    cameraSource.stop();


                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                    textView.setText(stringBuilder.toString());
                                    textView.setMovementMethod(new ScrollingMovementMethod());
                                }
                            }
                        });
                    }

                }
            });
        }
    }
    private void tts()
    {
        String s = String.valueOf(textView.getText().toString());
        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }
}