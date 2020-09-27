package com.example.android.storeinventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class StringListAdapter extends ArrayAdapter<String> {

    public StringListAdapter(Context context, ArrayList<String> list){
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;

        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.search_list_item_2, parent, false);
        }

        String currentString = getItem(position);

        TextView singleText = (TextView) listItemView.findViewById(R.id.single_text);
        singleText.setText(currentString);

        return listItemView;
    }
}
