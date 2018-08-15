package com.example.magnus.lab01a;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import golabpac.Golabpac;


/**
 * Activity A1 takes in an input text, and gives the user
 * a dropdown list of different hash algoritms to choose from.
 * When ok button is pressed the input text and hashed result
 * will be sendt to activity A2
 */
public class A1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);

        // L1 Spinner adding items
        Spinner spinner = (Spinner) findViewById(R.id.L1);
        ArrayAdapter<String> adapter;
        List<String> list;

        list = new ArrayList<String>();
        list.add("SHA256");
        list.add("SHA3-256");
        list.add("RIPEMD160");
        list.add("BLAKE2s-256");

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

     //Start activity A2 and hash text (linked in activity_a1.xml)
    public void activateA2(View view){
        Intent intent = new Intent(this, A2.class);
        EditText editText = (EditText) findViewById(R.id.T1);
        String textToHash = editText.getText().toString();

        Spinner spinner = (Spinner) findViewById(R.id.L1);

    try {
        String hashedText = Golabpac.hashString(spinner.getSelectedItem().toString(), textToHash);

        intent.putExtra("A1TextToHash", textToHash);
        intent.putExtra("A1HashedText", hashedText);
    }catch (Exception e){
        e.getStackTrace();
    }
        startActivity(intent);
    }
}