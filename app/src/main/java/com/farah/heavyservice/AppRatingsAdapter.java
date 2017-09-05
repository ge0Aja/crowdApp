package com.farah.heavyservice;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Georgi on 3/21/2017.
 */
public class AppRatingsAdapter extends BaseAdapter {

    private ArrayList mRatings;
    private Context mContext;

    public AppRatingsAdapter(Context context,HashMap<String,Float> map){
        mContext = context;
        mRatings = new ArrayList();
        mRatings.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mRatings.size();
    }

    @Override
    public Map.Entry<String,Float> getItem(int position) {
        return (Map.Entry) mRatings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map.Entry<String,Float> entry = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView tvAppName = (TextView) convertView.findViewById(android.R.id.text1);
        TextView tvAppRating = (TextView) convertView.findViewById(android.R.id.text2);

        tvAppName.setText(entry.getKey());
        tvAppRating.setText(entry.getValue().toString());

        tvAppName.setTextColor(Color.BLACK);
        tvAppRating.setTextColor(Color.BLACK);

        return convertView;
    }
}
