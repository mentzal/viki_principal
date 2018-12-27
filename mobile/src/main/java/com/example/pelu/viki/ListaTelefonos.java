package com.example.pelu.viki;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ListaTelefonos extends ListActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle("Choose a phone");

// Query: contacts with phone shorted by name

        Cursor mCursor = getContentResolver().query(

                ContactsContract.Data.CONTENT_URI,

            new String[] {ContactsContract.Data._ID , ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE },
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
                        + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null,
                ContactsContract.Data.DISPLAY_NAME + " ASC" );


        startManagingCursor(mCursor);


// Setup the list
        ListAdapter adapter = new SimpleCursorAdapter(this, // context
                android.R.layout.simple_list_item_2, // Layout for the rows


                mCursor, // cursor
                new String[] { ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER }, // cursor
// fields
                new int[] { android.R.id.text1, android.R.id.text2 } // view
// fields
        );
        setListAdapter(adapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {


// Get the data
        Cursor c = (Cursor) getListAdapter().getItem(position);
        int colIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        String phone = c.getString(colIdx);
        System.out.println("ESTE ES EL TELEOFNO SELECCIONAJDO" + phone.toString());


        Intent intent = new Intent(this, Dictado.class);
        intent.putExtra("phone", phone);
        startActivity(intent);

        finish();
    }




}
