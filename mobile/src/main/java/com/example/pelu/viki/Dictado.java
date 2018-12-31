package com.example.pelu.viki;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.cmu.pocketsphinx.Hypothesis;

public class Dictado extends Activity  {

    TextView grabar,numero;

    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictado);

        grabar = (TextView) findViewById(R.id.txtGrabarVoz2);
        numero= (TextView) findViewById(R.id.txtGrabarVoz);

       // final Button button = findViewById(R.id.img_btn_hablar);
       // final Button volver = findViewById(R.id.button2);
        //final Button enviar = findViewById(R.id.button3);
        final Button listaTlf = findViewById(R.id.button6);

        if(getIntent().getStringExtra("phone") != "" ||getIntent().getStringExtra("phone")!= null){

            String numero_telefono = getIntent().getStringExtra("phone");
            numero.setText(numero_telefono);
        }

        Intent intentActionRecognizeSpeech = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Configura el Lenguaje (Español-México)
        intentActionRecognizeSpeech.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        try {
            startActivityForResult(intentActionRecognizeSpeech,
                    RECOGNIZE_SPEECH_ACTIVITY);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Tú dispositivo no soporta el reconocimiento por voz",

                    Toast.LENGTH_SHORT).show();
        }


        listaTlf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent telefonos = new Intent(getApplicationContext(), ListaTelefonos.class);
                startActivity(telefonos);

                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent principal = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(principal);

        finish();


    }

    @Override
    protected void onStop() {

        super.onStop();

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

        String telefono = getIntent().getStringExtra("phone");
        openWhatsApp(grabar.getText().toString(),telefono);
    }

                                /*
           Abre whatsapp y envía mensaje al numero indicado
                                */
    public void openWhatsApp(String texto, String telefono){
        try {
            String text = texto ;// Replace with your message.

            String toNumber = telefono; // Replace with mobile phone number without +Sign or leading zeros.


            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
