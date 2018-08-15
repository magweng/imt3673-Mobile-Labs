package com.example.magnus.lab01a;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity A2 displays input text from activity 1
 * and shows the result of the hashed input text
 */
public class A2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2);

        Intent intent = getIntent();
        String textToHash = intent.getStringExtra("A1TextToHash");
        String hashedText = intent.getStringExtra("A1HashedText");

        TextView textView = findViewById(R.id.T2);
        textView.setText(textToHash);
        TextView hashedTextView = findViewById(R.id.T3);
        hashedTextView.setText(hashedText);
    }

    //Quit application (linked in activity_a2.xml)
    public void quitApp(View view){
        finishAffinity();
    }
}
