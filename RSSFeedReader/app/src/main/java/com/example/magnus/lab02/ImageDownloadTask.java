package com.example.magnus.lab02;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

/**
 * AsyncTask to download thumbnail images
 */
public class ImageDownloadTask extends AsyncTask<RSSItemView,Void,RSSItemView> {

    @Override
    protected RSSItemView doInBackground(RSSItemView... RSSItemViews) {

        if(RSSItemViews == null)
            return null;

        RSSItemView item = RSSItemViews[0];

        try{
            URL url = new URL(item.getImageUrl());
            item.setBitMap(BitmapFactory.decodeStream(url.openStream()));

        } catch (IOException e){
            // e.printStackTrace();
            item.setBitMap(null);
        }
        return item;
    }

    @Override
    protected void onPostExecute(RSSItemView result) {
        super.onPostExecute(result);

        if (result.getBitMap() == null) {
            result.getImageView().setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            result.getImageView().setImageBitmap(result.getBitMap());
        }
    }
}

