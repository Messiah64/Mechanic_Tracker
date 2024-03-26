package com.sp.mechanictracker;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private List<String> items;
    private Context context;

    public CustomSpinnerAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(items.get(position));
        textView.setTextColor(Color.BLACK); // Set text color to black

        // Set background color based on the item position
        switch (position) {
            case 0: // Gold
                textView.setBackgroundColor(Color.parseColor("#FFD700")); // Gold color
                break;
            case 1: // Premium
                textView.setBackgroundColor(Color.parseColor("#C0C0C0")); // Silver color
                break;
            case 2: // Safety
                textView.setBackgroundColor(Color.parseColor("#CD7F32")); // Bronze color
                break;
            case 3: // Rec
                textView.setBackgroundColor(Color.parseColor("#FFC0CB")); // Pink color
                break;
            case 4: // Nil
            default:
                textView.setBackgroundColor(Color.WHITE); // Plain white color
                break;
        }

        return textView;
    }
}
