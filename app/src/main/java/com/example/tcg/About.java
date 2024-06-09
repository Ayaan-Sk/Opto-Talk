package com.example.tcg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class About extends AppCompatActivity {
 TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                    int language = textToSpeech.setLanguage(Locale.ENGLISH);
                    String welcomeMessage = "Welcome to OptoTalk App\n" +
                            "\n" +
                            "Point the camera towards the area you want to capture , Image will be captured automatically after 5 seconds , if you are not satisfied or you want to click the image again press volume up key twice / 2 times to capture the image again.\n" +
                            "\n" +
                            "to listen the extracted text , press volume key down once , to listen again press the volume key down again\n" +
                            "\n" +
                            "to close the app, press the volume key down twice and your application will be automatically get closed."; // Your welcome message
                    float speechRate = 0.7f; // Adjust the value as needed
                    textToSpeech.setSpeechRate(speechRate);
                    textToSpeech.speak(welcomeMessage, TextToSpeech.QUEUE_FLUSH, null, null);

                }
            });
        }
}
