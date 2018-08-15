package com.example.magnus.chatapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * User tab that holds list over users,
 * and open user message window if one user is clicked
 */

public class UsersFragment extends Fragment {

    private final ArrayList<String> users = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private final UserReceiver mReceiver = new UserReceiver();
    private final IntentFilter mIntentFilter = new IntentFilter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.users_fragment_layout, container,false);

        ListView list = view.findViewById(R.id.users_user_list);

        if(getActivity() != null)
            this.mAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),android.R.layout.simple_list_item_1,users);

        list.setAdapter(this.mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Start intent with username @ position in users list

                Intent intent = new Intent(view.getContext(), UserMessage.class);
                intent.putExtra(UserMessage.USER_MESSAGE, users.get(position));
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * Registers user receiver on resume
     */
    @Override
    public void onResume() {
        this.mIntentFilter.addAction(MessageService.USER);

        if(getActivity() != null)
            getActivity().registerReceiver(this.mReceiver,this.mIntentFilter);

        super.onResume();
    }


    /**
     * Receives users from MessageService
     */
    private class UserReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List users = intent.getStringArrayListExtra(MessageService.USER_USERS);

            Log.i("UserReceiver",users.toString());

            mAdapter.clear();
            for(int i = 0; i < users.size(); i++ ){
                mAdapter.add(users.get(i).toString());
            }
        }
    }

    /**
     * unregister receiver onDestroy
     */
    @Override
    public void onDestroy() {
        if(getActivity() != null)
            getActivity().unregisterReceiver(this.mReceiver);

        super.onDestroy();
    }


}
