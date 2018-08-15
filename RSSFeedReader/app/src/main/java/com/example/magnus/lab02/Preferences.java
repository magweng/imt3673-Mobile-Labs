package com.example.magnus.lab02;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Preferences class. Sets preferences for the rss feed
 */
public class Preferences extends AppCompatActivity {

    public static final String PREFS_NAME       = "MyPrefsFile";
    public static final String URL              = "url";
    public static final String NUM_ITEM_POS     = "numItemPos";
    public static final String NUM_ITEM_VALUE   = "numItemsValue";
    public static final String UPDATE           = "update";
    public static final String REFRESH_POS      = "refresh";
    public static final String RATE             = "rate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Do not display back button in actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);

        ((EditText) findViewById(R.id.T1)).setText(prefs.getString(URL, ""));
        ((Spinner) findViewById(R.id.S1)).setSelection(prefs.getInt(NUM_ITEM_POS,0));
        ((Spinner) findViewById(R.id.S2)).setSelection(prefs.getInt(REFRESH_POS,0));
    }


    /**
     * Saves preferences and sends values to itemList
     */
    public void savePreferences(View view) {

        final String feedUrl = ((EditText) findViewById(R.id.T1)).getText().toString();

        if (validURL(feedUrl)) {
            final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            final SharedPreferences.Editor editor = prefs.edit();

            final Spinner sp1 = findViewById(R.id.S1);
            final Spinner sp2 = findViewById(R.id.S2);

            // Saving states
            editor.putString(URL, feedUrl);
            editor.putInt(NUM_ITEM_POS, sp1.getSelectedItemPosition());
            editor.putString(NUM_ITEM_VALUE, sp1.getSelectedItem().toString());
            editor.putInt(REFRESH_POS, sp2.getSelectedItemPosition());
            editor.apply();

            // SEND RESPONSE WITH item to ItemList
            Intent resultIntent = new Intent();
            resultIntent.putExtra(UPDATE, true);
            resultIntent.putExtra(NUM_ITEM_VALUE, sp1.getSelectedItem().toString());
            resultIntent.putExtra(URL,feedUrl);
            resultIntent.putExtra(RATE, sp2.getSelectedItemPosition());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    /**
     * Checks if URL is valid
     * @param urlString urlString to check
     * @return true if valid false if not
     */
        private Boolean validURL(final String urlString){
            Boolean success = true;
            URL url;

            if(urlString.isEmpty()){
                Toast.makeText(getApplicationContext(),"Must type Url", Toast.LENGTH_LONG).show();
                success = false;
            }
            try {
                url = new URL(urlString);
                if(!Patterns.WEB_URL.matcher(urlString).matches() && URLUtil.isValidUrl(urlString)) {
                    Toast.makeText(getApplicationContext(),"URL is not working", Toast.LENGTH_LONG).show();
                    success = false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"URL is not working", Toast.LENGTH_LONG).show();
                success = false;
            }
            return success;
        }
    }

