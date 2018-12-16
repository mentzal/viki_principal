package com.example.pelu.viki;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.cmu.pocketsphinx.Hypothesis;

public class Dictado extends Activity  {

    TextView grabar;

    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictado);

        grabar = (TextView) findViewById(R.id.txtGrabarVoz);
        final ImageButton button = findViewById(R.id.img_btn_hablar);
        final Button volver = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                Intent intentActionRecognizeSpeech = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                // Configura el Lenguaje (Español-México)
                intentActionRecognizeSpeech.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX");
                try {
                    startActivityForResult(intentActionRecognizeSpeech,
                            RECOGNIZE_SPEECH_ACTIVITY);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Tú dispositivo no soporta el reconocimiento por voz",

                            Toast.LENGTH_SHORT).show();
                }

            }
        });


        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(Dictado.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        switch (requestCode) {
            case RECOGNIZE_SPEECH_ACTIVITY:

                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> speech = data
                            .getStringArrayListExtra(RecognizerIntent.
                                    EXTRA_RESULTS);
                    String strSpeech2Text = speech.get(0);

                    grabar.setText(strSpeech2Text);
                }

                break;
            default:

                break;
        }
    }



    public void onClickImgBtnHablar(View v) {

        Intent intentActionRecognizeSpeech = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

// Configura el Lenguaje (Español-México)
        intentActionRecognizeSpeech.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX");
        try {
            startActivityForResult(intentActionRecognizeSpeech,
                    RECOGNIZE_SPEECH_ACTIVITY);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Tú dispositivo no soporta el reconocimiento por voz",

                    Toast.LENGTH_SHORT).show();
        }

    }


}
