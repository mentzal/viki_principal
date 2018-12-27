package com.example.pelu.viki;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class ListaTelefonos extends ListActivity implements SearchView.OnQueryTextListener{


    EditText inputSearch;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle("Choose a phone");

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        


// Query: contacts with phone shorted by name

        Cursor mCursor = getContentResolver().query(

                ContactsContract.Data.CONTENT_URI,

                new String[] {ContactsContract.Data._ID , ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE },
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
                        + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null,
                ContactsContract.Data.DISPLAY_NAME + " ASC" );


        startManagingCursor(mCursor);



         final ListAdapter adapter = new SimpleCursorAdapter(this, // context
                android.R.layout.simple_list_item_2, // Layout for the rows

                mCursor, // cursor
                new String[] { ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER }, // cursor

                new int[] { android.R.id.text1, android.R.id.text2 } // view

        );



        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setListAdapter(adapter);




    }





    protected void onListItemClick(ListView l, View v, int position, long id) {

        Cursor c = (Cursor) getListAdapter().getItem(position);
        int colIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        String phone = c.getString(colIdx);
        System.out.println("ESTE ES EL TELEOFNO SELECCIONAJDO" + phone.toString());


        Intent intent = new Intent(this, Dictado.class);
        intent.putExtra("phone", phone);
        startActivity(intent);

        finish();
    }


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

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
