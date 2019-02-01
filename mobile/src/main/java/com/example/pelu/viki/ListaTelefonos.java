package com.example.pelu.viki;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.Assets;

public class ListaTelefonos extends AppCompatActivity implements SearchView.OnQueryTextListener {


    EditText inputSearch;
    private ArrayAdapter<String> adapters;
    private  String[] parts;
    private String part1;
    private String part2;
    private Boolean voz ;
    private String textoGrabado;
    ListView listaTelefonos;

    MenuItem MusicaPlay, MusicaNext, MusicaStop;

    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);


        inputSearch = (EditText) findViewById(R.id.inputSearch);
        ArrayList<String> datos = new ArrayList<String>();
        listaTelefonos = (ListView) findViewById(R.id.list);
        MusicaNext = (MenuItem) findViewById(R.id.action_shuffle);
        MusicaStop = (MenuItem) findViewById(R.id.action_end);





        Cursor mCursor = getContentResolver().query(

                ContactsContract.Data.CONTENT_URI,

                new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE},
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
                        + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL" , null,
                ContactsContract.Data.DISPLAY_NAME + " ASC");


        while(mCursor.moveToNext()) {

            datos.add(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                    + "\n" + mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));


        }

                        /*
        Depuracion de la consulta para eliminar los
        telefonos duplicados..... Mejoorado
                        */

        for(int i = 0; i<datos.size(); i++){

            String split[] = datos.get(i).split("\n");

            for (int j = 0; j<datos.size(); j++){

                String splitj[] = datos.get(j).split("\n");
                if(splitj[0].equals(split[0]) || datos.get(i).equals(datos.get(j))){


                    datos.remove(datos.get(j));


                }
            }


        }
            startManagingCursor(mCursor);


        adapters = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, datos);

        listaTelefonos.setAdapter(adapters);

        listaTelefonos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                voz = false;

            //todo: obtener el telefono para pasarlo a la activity y cerrar esta //
               // System.out.println(adapters.getItem(position));
                  String fichaContacto = adapters.getItem(position);

                 parts = fichaContacto.split("\\n");
                 part1 = parts[0]; // NOMBRE
                 part2 = parts[1]; // Numero

                System.out.println(" PARTE 1" + part1);
                System.out.println(" PARTE2 " + part2);

                                         /*
        Reconocimiento de voz para buscar los contactos
                                */

                Intent intentActionRecognizeSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                // Configura el Lenguaje (Español-México)
                intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
                intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT,"Texto a enviar");

                try {
                    startActivityForResult(intentActionRecognizeSpeech, RECOGNIZE_SPEECH_ACTIVITY);

                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),"Tú dispositivo no soporta el reconocimiento por voz",  Toast.LENGTH_SHORT).show();

                }

            }
        });

        inputSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                voz= true;
                                 /*
        Reconocimiento de voz para buscar los contactos
                                */

                Intent intentActionRecognizeSpeech = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                // Configura el Lenguaje (Español-México)
                intentActionRecognizeSpeech.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
                intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT,"Busca un contacto");
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


        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            adapters.getFilter().filter(s);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                adapters.getFilter().filter(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapters.getFilter().filter(s);
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

                    if(voz == false){

                        textoGrabado = strSpeech2Text;

                                            /*
                        Abre whatsup y vuelve a la activity principal
                                            */

                        new AsyncTask<Void, Void, Exception>() {
                            @Override
                            protected Exception doInBackground(Void... params) {
                                try {
                                    openWhatsApp(textoGrabado,part2);



                                    finish();
                                } catch (Exception e) {

                                    return e;
                                }
                                return null;
                            }

                        }.execute();

                        Intent principal = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(principal);

                    }
                    else if (voz == true){
                        inputSearch.setText(strSpeech2Text);

                    }

                }
                break;
            default:

                break;
        }
    }

    /*
        Abre whatsapp y envía mensaje al numero indicado
                             */
    public void openWhatsApp(String texto, String telefono){
        try {
            String text = texto ;// Replace with your message.

            String toNumber = telefono; // Replace with mobile phone number without +Sign or leading zeros.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(telefono.startsWith("+34")){
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            }
            else{
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=+34"+toNumber +"&text="+text));
            }



            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem sufle = menu.findItem(R.id.action_shuffle);
        MenuItem salir = menu.findItem(R.id.action_end);


        sufle.setVisible(false);
        salir.setVisible(false);

        MenuItem off = menu.findItem(R.id.estadoVikiOff);
        MenuItem on = menu.findItem(R.id.estadoVikiOn);
        on.setVisible(false);



        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        return super.onOptionsItemSelected(item);
    }

        @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
