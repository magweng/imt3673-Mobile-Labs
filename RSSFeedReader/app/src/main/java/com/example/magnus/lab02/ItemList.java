package com.example.magnus.lab02;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * MainActivity, starts FetchRSSFeedService when given an url
 * if not url is set user will be send to Preferences activity.
 *
 * Signals service when onPause to not send updated feed,
 * Signals service on resume to send updated feed if the feed has been updated in that time
 */
public class ItemList extends AppCompatActivity {
    final static String LISTENER_UP   = "LISTENERUP";
    final static String AWAKE         = "isUp";
    final static String LINK          = "link";
    private final static int    REQUEST_CODE  = 1;

    private ArrayList headlines            = new ArrayList();
    private ArrayList descriptions         = new ArrayList();
    private ArrayList links                = new ArrayList();
    private ArrayList imageUrls            = new ArrayList();

    private final MyReceiver   myReceiver           = new MyReceiver();
    private final IntentFilter intentFilter         = new IntentFilter();
    private Boolean            serviceIsRunning     = false;
    private Boolean            receiverIsRegistered = false;

    private ListAdapter  adapter;
    private int          numOfItemsDisp;
    private int          fetchRate;
    private String       url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        initItemList();

        // Settings button
        findViewById(R.id.B1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), Preferences.class), REQUEST_CODE);
            }
        });

        // Setting up receiver
        this.intentFilter.addAction(FetchRSSFeedService.ACTION);
        registerReceiver(myReceiver,intentFilter);
        this.receiverIsRegistered = true;

        // Get url if it exists
        SharedPreferences prefs = getSharedPreferences(Preferences.PREFS_NAME, 0);
        this.url = prefs.getString(Preferences.URL, "");

        // If no Url is set, Preferences activity will start.
        if(url.isEmpty()){
            startActivityForResult(new Intent(this, Preferences.class), REQUEST_CODE);
        } else {
            this.numOfItemsDisp = Integer.parseInt(prefs.getString(Preferences.NUM_ITEM_VALUE,""));
            startRSSFeedService();
            this.serviceIsRunning = true;
        }

    }

    /**
     * Preferences have been updated, will then restart service to use new config
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                final boolean preferencesUpdated = data.getBooleanExtra(Preferences.UPDATE,false);

                if(preferencesUpdated){
                    this.numOfItemsDisp = Integer.parseInt(data.getStringExtra(Preferences.NUM_ITEM_VALUE));

                    url = data.getStringExtra(Preferences.URL);
                    fetchRate = data.getIntExtra(Preferences.RATE,0);

                    if(serviceIsRunning) {
                        stopRSSFeedService();
                    }
                    startRSSFeedService();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        // Tell FetchRSSFeedService that im sleeping
        signalService(false);

        unregisterReceiver(this.myReceiver);
        this.receiverIsRegistered = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(myReceiver,intentFilter);
        this.receiverIsRegistered = true;

        // Tell FetchRSSFeedService that im awake
        signalService(true);

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopRSSFeedService();

        if(this.receiverIsRegistered){
            unregisterReceiver(this.myReceiver);
        }
        super.onDestroy();
    }

    /**
     * Sends signal to service that activity is paused or resumed
     * @param awake true if awake false if not
     */
    private void signalService(final Boolean awake) {
        Intent intent = new Intent();
        intent.setAction(LISTENER_UP);
        intent.putExtra(AWAKE,awake);
        sendBroadcast(intent);
    }

    /**
     * Start FetchRSSFeedService service with url and fetch rate
     */
    private void startRSSFeedService() {
        Intent serviceIntent = new Intent(this,FetchRSSFeedService.class);
        serviceIntent.putExtra(Preferences.URL, this.url);
        serviceIntent.putExtra(Preferences.RATE,this.fetchRate);
        startService(serviceIntent);
    }

    /**
     * Stop FetchRSSFeedService service
     */
    private void stopRSSFeedService() {
        Intent serviceIntent = new Intent(this,FetchRSSFeedService.class);
        stopService(serviceIntent);
    }

    /**
     * Initialise ListView
     */
    private void initItemList() {
        adapter = new ListAdapter(this,this.headlines,this.descriptions,this.imageUrls);
        ListView itemList = findViewById(R.id.L1);
        itemList.setAdapter(this.adapter);
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //https://developer.android.com/reference/android/widget/AdapterView.OnItemClickListener.html
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {

                final Intent intent = new Intent(ItemList.this, DisplayItem.class);

                final String link = (String)links.get(position);

                intent.putExtra(LINK, link);
                startActivity(intent);
            }
        });
    }

    /**
     * Receives broadcast from FetchRSSFeedService service
     * and updated ListView with new data
     */
    private class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            headlines.clear();
            links.clear();
            adapter.clear();
            imageUrls.clear();
            headlines       = intent.getStringArrayListExtra(FetchRSSFeedService.HEADERS);
            links           = intent.getStringArrayListExtra(FetchRSSFeedService.LINKS);
            descriptions    = intent.getStringArrayListExtra(FetchRSSFeedService.DESC);
            imageUrls       = intent.getStringArrayListExtra(FetchRSSFeedService.IMAGE);

            // This is just a hacky solution if image tag does not exist. filling array with null
            if(imageUrls.isEmpty()){
                for (int i = 0; i < headlines.size(); i++)
                    imageUrls.add(null);
            }

            // Items Displayed
            for (int i = 0; i < ((numOfItemsDisp > headlines.size()) ? headlines.size() : numOfItemsDisp); i++)
                adapter.add((String)headlines.get(i),(String)descriptions.get(i),(String)imageUrls.get(i));
        }
    }
}






