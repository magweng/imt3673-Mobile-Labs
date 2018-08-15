package com.example.magnus.chatapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserMessage extends AppCompatActivity {
    public static final String  USER_MESSAGE = "user_message";

    private String              mUsername;
    private ListAdapterMessages mMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_message);

        this.mUsername = getIntent().getStringExtra(USER_MESSAGE);
        ((TextView) findViewById(R.id.tv_username)).setText(mUsername);

        initListView();
        getMessagesForUsername();

    }

    /**
     * Get all messages for his username from database
     */
    private void getMessagesForUsername() {
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        mDatabase.collection("messages")
                //.whereEqualTo("u", mUsername) does not work with order by
                .orderBy("d")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int numberOfMessages = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                String user = document.get("u").toString();
                                if(user.equalsIgnoreCase(mUsername)){
                                    String dateString;
                                    if(document.get("d") != null) {
                                        Date date =  (Date)document.get("d");
                                        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
                                        dateString = sfd.format(date);
                                    } else {
                                        dateString = "no date";
                                    }
                                    numberOfMessages++;
                                    mMessagesAdapter.add(new Message(dateString,document.get("u").toString(),document.get("m").toString()));
                                }
                            }
                            if(numberOfMessages < 1){
                                findViewById(R.id.message_from_user_list).setVisibility(View.INVISIBLE);
                                findViewById(R.id.tv_no_messages).setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("UserMessage", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Initialize list view
     */
    private void initListView() {
        ArrayList<Message> mMessages = new ArrayList<>();
        ListView mListView = findViewById(R.id.message_from_user_list);
        this.mMessagesAdapter = new ListAdapterMessages(this, mMessages);
        mListView.setAdapter(this.mMessagesAdapter);
    }
}
