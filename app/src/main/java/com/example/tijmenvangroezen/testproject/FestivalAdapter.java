package com.example.tijmenvangroezen.testproject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tijmenvangroezen on 03-10-16.
 */

public class FestivalAdapter extends ArrayAdapter<FestivalEvent>
{
    public FestivalAdapter(Context context, int resource, int textViewResourceId, List<FestivalEvent> events)
    {
        super(context, resource, textViewResourceId, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = super.getView(position, convertView, parent);

        TextView titleView = (TextView) rowView.findViewById(R.id.listview_item_title);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.listview_item_description);

        titleView.setText(getItem(position).title);
        descriptionView.setText(getItem(position).description_teaser);

        return rowView;
    }
}
