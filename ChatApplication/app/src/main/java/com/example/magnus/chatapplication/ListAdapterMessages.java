package com.example.magnus.chatapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom array adapter for messages
 * https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 */

class ListAdapterMessages extends ArrayAdapter<Message>{

    public ListAdapterMessages(Context context, ArrayList<Message> messages) {
        super(context,0,messages);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Message message = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
        }

        TextView userNameView = convertView.findViewById(R.id.item_user);
        TextView messageView = convertView.findViewById(R.id.item_message);
        TextView dateView = convertView.findViewById(R.id.item_date);

        userNameView.setText(message != null ? message.getUserNickName() : null);
        messageView.setText(message != null ? message.getMessage() : null);
        dateView.setText(message != null ? message.getDate() : null);

        return convertView;
    }
}
