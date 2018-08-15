package com.example.magnus.lab02;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * FetchRSSFeedService service, uses a timer to execute AsyncTask "FetchFeed"
 * Sends updated feed to itemList activity when itemList is active
 */
public class FetchRSSFeedService extends Service {

    final static String ACTION          = "ACTION";
    final static String HEADERS         = "HEADERS";
    final static String LINKS           = "LINKS";
    final static String DESC            = "DESC";
    final static String IMAGE           = "IMAGE";

    private final int TEN_MINUTES       = 600000;
    private final int ONE_HOUR          = 3600000;
    private final int TWELVE_HOURS      = 12 * ONE_HOUR;

    private final ArrayList<String> headlines         = new ArrayList<String>();
    private final ArrayList<String> links             = new ArrayList<String>();
    private final ArrayList<String> descriptions      = new ArrayList<String>();
    private final ArrayList<String> imageLink         = new ArrayList<String>();
    private final Timer             timer             = new Timer();
    private final FeedReceiver      feedReceiver      = new FeedReceiver();
    private final IntentFilter      intentFilter      = new IntentFilter();
    private Boolean                 itemReceiverIsUp  = true;
    private Boolean                 feedUpdated       = false;

    private String                  urlString;
    private int                     fetchRate;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.urlString = intent.getStringExtra(Preferences.URL);
        this.fetchRate = intent.getIntExtra(Preferences.RATE, 0);

        setFetchTimer();

        this.intentFilter.addAction(ItemList.LISTENER_UP);
        registerReceiver(feedReceiver,intentFilter);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(feedReceiver);
        super.onDestroy();
    }


    /**
     * Get input stream from Url
     * @param url url
     * @return stream from url
     */
    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Send updated feed to ItemList activity
     */
    private void sendUpdate(){
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putStringArrayListExtra(HEADERS,this.headlines);
        intent.putStringArrayListExtra(LINKS,this.links);
        intent.putStringArrayListExtra(DESC,this.descriptions);
        intent.putStringArrayListExtra(IMAGE,this.imageLink);
        sendBroadcast(intent);
    }

    /**
     * Sets timer to fetch feed
     */
    private void setFetchTimer() {
        TimerTask fetch = new TimerTask() {
            @Override
            public void run() {
                new FetchFeed().execute();
            }
        };

        switch (fetchRate){
            case 0:  timer.scheduleAtFixedRate(fetch, 0, TEN_MINUTES);
                break;
            case 1:  timer.scheduleAtFixedRate(fetch, 0, ONE_HOUR);
                break;
            case 2:  timer.scheduleAtFixedRate(fetch, 0, TWELVE_HOURS);
                break;
        }
    }
    /**
     * Receiver that receives signal if ItemList is active and sends update
     */
    private class FeedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            itemReceiverIsUp = intent.getBooleanExtra(ItemList.AWAKE, false);

            if(itemReceiverIsUp && feedUpdated){
                sendUpdate();
                feedUpdated = false;
            }
        }
    }

    /**
     * Inner class FetchFeed extends AsyncTask
     * Fetches feed from url
     * Yes I realized that i could have user just a simple thread instead of AsyncTask
     */
    private class FetchFeed extends AsyncTask<Void, Void, Void> {
        // Used to set url in Preferences to "" if link does not work
        final SharedPreferences prefs = getSharedPreferences(Preferences.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = prefs.edit();

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL(urlString);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(getInputStream(url), "UTF_8");

                boolean insideItem = false;

                // For imageUrl inside enclosure
               //  String imgUrl = new String();

                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item") || xpp.getName().equalsIgnoreCase("entry")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem)
                                headlines.add(xpp.nextText());
                        } else if (xpp.getName().equalsIgnoreCase("link")  ) {
                            if (insideItem)
                                links.add(xpp.nextText());
                        } else if (xpp.getName().equalsIgnoreCase("description") || xpp.getName().equalsIgnoreCase("subtitle") || xpp.getName().equalsIgnoreCase("summary") ) {
                            if (insideItem)
                                descriptions.add(xpp.nextText()); //Image url
                        } else if (xpp.getName().equalsIgnoreCase("image") || xpp.getName().equalsIgnoreCase("logo")) { // xpp.getName().equalsIgnoreCase("enclosure") // Testing
                            if (insideItem)
                               // if(xpp.getName().equalsIgnoreCase("enclosure")) { // Testing
                                //    imgUrl = xpp.getAttributeValue(null, "url");
                                 //   imageLink.add(imgUrl);
                                 //   imgUrl = new String();
                                //} else
                                imageLink.add(xpp.nextText());
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }
                    eventType = xpp.next(); //move to next element
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                urlString = null;       // This is just in case the app crashes and keeps loading a link that does not work
                this.editor.putString(Preferences.URL, "");
                this.editor.apply();
                return null;
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                urlString = null;
                this.editor.putString(Preferences.URL, "");
                this.editor.apply();
            }

            return null;
        }

        /**
         * Sends updated data to ItemList if ItemList is awake
         * Keeps data if ItemList is onPause()
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(headlines.size() < 1){
                Toast.makeText(getApplicationContext(),"Link does not work!", Toast.LENGTH_LONG).show();
                urlString = null;
                this.editor.putString(Preferences.URL, "");
                this.editor.apply();
            } else {
                if(itemReceiverIsUp){
                    sendUpdate();
                } else {
                    feedUpdated = true;
                }
            }
        }
    }

}
