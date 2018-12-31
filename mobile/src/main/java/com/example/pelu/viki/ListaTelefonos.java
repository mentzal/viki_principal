package com.example.pelu.viki;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListaTelefonos extends AppCompatActivity implements SearchView.OnQueryTextListener {


    EditText inputSearch;
    private ArrayAdapter<String> adapters;
    ListView listaTelefonos;

    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle("Choose a phone");

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        ArrayList<String> datos = new ArrayList<String>();
        listaTelefonos = (ListView) findViewById(R.id.list);

        Cursor mCursor = getContentResolver().query(

                ContactsContract.Data.CONTENT_URI,

                new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE},
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
                        + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null,
                ContactsContract.Data.DISPLAY_NAME + " ASC");


        while(mCursor.moveToNext()) {
            datos.add(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                     + "\n" + mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

        }
            startManagingCursor(mCursor);


        adapters = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, datos);

        listaTelefonos.setAdapter(adapters);

        listaTelefonos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            //todo: obtener el telefono para pasarlo a la activity y cerrar esta //
               // System.out.println(adapters.getItem(position));
                  String fichaContacto = adapters.getItem(position);

                String[] parts = fichaContacto.split("\\n");
                String part1 = parts[0]; // NOMBRE
                String part2 = parts[1]; // Numero

                System.out.println(" PARTE 1" + part1);
                System.out.println(" PARTE2 " + part2);

                Intent intent = new Intent(getApplicationContext(), Dictado.class);
                intent.putExtra("phone", part2);
                startActivity(intent);

                finish();

            }
        });


        inputSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                                 /*
        Reconocimiento de voz para buscar los contactos
                                */

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

                    inputSearch.setText(strSpeech2Text);
                }

                break;
            default:

                break;
        }
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        */
        return true;
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
