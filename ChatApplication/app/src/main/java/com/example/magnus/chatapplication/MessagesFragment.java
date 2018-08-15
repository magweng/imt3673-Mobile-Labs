package com.example.magnus.chatapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Message fragment containing message list and are able to write messages
 */

public class MessagesFragment extends Fragment {

    private ListAdapterMessages mMessagesAdapter;
    private View mRootView;
    private FirebaseFirestore mDatabase;
    private final MessageReceiver mMessageReceiver = new MessageReceiver();
    private final IntentFilter mIntentFilter = new IntentFilter();
    private final int MAX_CHAR = 256;

    private TabbedActivity mParentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.messages_fragment_layout, container,false);
        this.mParentActivity = (TabbedActivity)getActivity(); // Used to get username

        initMessageUI();
        initListView();
        InitSendButton();
        this.mDatabase = FirebaseFirestore.getInstance();

        return this.mRootView;
    }

    /**
     * Initializes message ui behavior when sending a message
     * https://stackoverflow.com/questions/10699202/how-to-change-textviews-text-on-change-of-edittexts-text
     */
    private void initMessageUI() {

        (mRootView.findViewById(R.id.message_return)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               changeUIBackFromMessage();
            }
        });

        EditText inputText = mRootView.findViewById(R.id.messages_input_text);
        TextWatcher inputTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                TextView textView = mRootView.findViewById(R.id.write_message_view);
                textView.setText(s.toString());
                String count = getResources().getString(R.string.character_count) + textView.getText().toString().length();
                ((TextView)mRootView.findViewById(R.id.char_counter_label)).setText(count);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    (mRootView.findViewById(R.id.message_list)).setVisibility(View.INVISIBLE);
                    (mRootView.findViewById(R.id.write_message_view)).setVisibility(View.VISIBLE);
                    (mRootView.findViewById(R.id.message_return)).setVisibility(View.VISIBLE);
                    (mRootView.findViewById(R.id.char_counter_label)).setVisibility(View.VISIBLE);

                } else {
                    changeUIBackFromMessage();
                }
            }
        });
        inputText.addTextChangedListener(inputTextWatcher);
    }

    /**
     * Changes UI back if in message "mode"
     */
    private void changeUIBackFromMessage() {
        (mRootView.findViewById(R.id.message_list)).setVisibility(View.VISIBLE);
        (mRootView.findViewById(R.id.write_message_view)).setVisibility(View.INVISIBLE);
        (mRootView.findViewById(R.id.message_return)).setVisibility(View.INVISIBLE);
        (mRootView.findViewById(R.id.char_counter_label)).setVisibility(View.INVISIBLE);
        ((TextView)mRootView.findViewById(R.id.messages_input_text)).setText("");
        (mRootView.findViewById(R.id.messages_input_text)).clearFocus();
    }

    /**
     * Initialize list view
     */
    private void initListView() {
        ArrayList<Message> mMessages = new ArrayList<>();
        ListView mListView = this.mRootView.findViewById(R.id.message_list);
        this.mMessagesAdapter = new ListAdapterMessages(this.mRootView.getContext(), mMessages);
        mListView.setAdapter(this.mMessagesAdapter);
    }

    /**
     * Initialize send button
     */
    private void InitSendButton() {
        Button sendButton = this.mRootView.findViewById(R.id.messages_b1_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView inputText = mRootView.findViewById(R.id.messages_input_text);

                if(!inputText.getText().toString().isEmpty()){
                    if(inputText.getText().toString().length() < MAX_CHAR){
                        // create message
                        Map<String, Object> message = new HashMap<>();
                        message.put("d", FieldValue.serverTimestamp());
                        message.put("u", mParentActivity.getUserName());
                        message.put("m", inputText.getText().toString());

                        //Send message to db
                        mDatabase.collection("messages")
                                .add(message)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("db", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("db", "Error adding document", e);
                                    }
                                });

                        inputText.setText("");
                        changeUIBackFromMessage();
                        inputText.clearFocus();

                    } else {
                        Toast.makeText(getContext(),"MAX 256 characters",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getContext(),"Message field is empty",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Receives data from MessageService
     */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List date = intent.getStringArrayListExtra(MessageService.MESSAGE_DATE);
            ArrayList<String> user = intent.getStringArrayListExtra(MessageService.MESSAGE_USER);
            List message = intent.getStringArrayListExtra(MessageService.MESSAGE_MESSAGE);

            Log.i("listener","Inside receiver");

            Log.i("FragmentReceivedMessage",date.toString());
            Log.i("FragmentReceivedMessage",user.toString());
            Log.i("FragmentReceivedMessage",message.toString());

            mMessagesAdapter.clear();
            for(int i = 0; i < date.size(); i++ ){

                if(date.get(i).toString().equalsIgnoreCase("no date")){ // Time stamp in firestore may not have been updated so use local https://stackoverflow.com/questions/47771044/firestore-timestamp-getting-null
                    Date nullDate = new Date();
                    SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
                    mMessagesAdapter.add(new Message(sfd.format(nullDate), user.get(i),message.get(i).toString()));
                } else {
                    mMessagesAdapter.add(new Message(date.get(i).toString(), user.get(i),message.get(i).toString()));
                }
            }
        }
    }

    /**
     * Sends message to service that receiver is up
     */
    @Override
    public void onResume() {
        this.mIntentFilter.addAction(MessageService.MESSAGE_SERVICE);
        changeUIBackFromMessage();

        if(getActivity() != null)
            getActivity().registerReceiver(this.mMessageReceiver,this.mIntentFilter);

        Intent intent = new Intent();
        intent.setAction(MessageService.ALIVE_SIGNAL);
        intent.putExtra(MessageService.WAKEUP_TYPE,"WAKEUP");
        intent.putExtra(MessageService.WAKEUP_SIGNAL,true);
        getActivity().sendBroadcast(intent);
        super.onResume();
    }

    /**
     * Unregister receiver onPause
     */
    @Override
    public void onPause() {

        if(getActivity() != null)
            getActivity().unregisterReceiver(this.mMessageReceiver);
        super.onPause();

    }

    /**
     * unregister receiver if not unregistered onDestroy
     */
    @Override
    public void onDestroy() {

        if(getActivity() != null){
            try{
                getActivity().unregisterReceiver(this.mMessageReceiver);
            } catch (IllegalArgumentException  e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

}
