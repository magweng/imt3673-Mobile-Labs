package com.example.magnus.lab02;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter for the listview
 * Inspiration http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/
 */

// from http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/
public class ListAdapter extends ArrayAdapter<String> {
    private final Activity          context;
    private final ArrayList<String> itemName;
    private final ArrayList<String> description;
    private final ArrayList<String> imageStreams;


    public ListAdapter(Activity context, ArrayList<String> title, ArrayList<String> description, ArrayList<String> image) {
        super(context, R.layout.rss_item, title);

        this.context = context;
        this.itemName = title;
        this.description = description;
        this.imageStreams = image;
    }

    /**
     * Setting rss_item view and download thumbnail if exist
     */
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView            = inflater.inflate(R.layout.rss_item, null,true);
        RSSItemView itemView    = new RSSItemView();

        itemView.setTitle((TextView) rowView.findViewById(R.id.titleText));
        itemView.setDescription((TextView) rowView.findViewById(R.id.description));
        itemView.setImageView((ImageView) rowView.findViewById(R.id.thumbnail));
        itemView.setImageUrl(imageStreams.get(position));

        itemView.getTitle().setText(itemName.get(position));
        itemView.getDescription().setText(description.get(position));

        rowView.setTag(itemView);

        //Adding placeholder thumbnail if imageUrl does not exist
        if (itemView.getImageUrl() != null)
            new ImageDownloadTask().execute(itemView);
        else
            itemView.getImageView().setImageResource(R.drawable.ic_launcher_foreground);

        return rowView;
    }

    /**
     * Add item to adapter
     * @param title title
     * @param desc description
     * @param image imageUrl
     */
    public void add(String title, String desc, String image) {
        this.itemName.add(title);
        this.description.add(desc);
        this.imageStreams.add(image);
        notifyDataSetChanged();
    }

    /**
     * Clear data in adapter
     */
    public void clear() {
        this.itemName.clear();
        this.description.clear();
        this.imageStreams.clear();
    }
}
