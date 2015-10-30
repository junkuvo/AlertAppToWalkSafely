package com.example.okubo.onsenkensaku;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class ListItemAdapter extends ArrayAdapter<OnsenData> {
    private LayoutInflater layoutInflater_;
    private Activity mActivity;

    public ListItemAdapter(Context context, int textViewResourceId, List<OnsenData> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActivity = (Activity)context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OnsenData item = (OnsenData)getItem(position);

        if (null == convertView ) {
            convertView = layoutInflater_.inflate(R.layout.list_item, null);
        }

        TextView textView;
        textView = (TextView) convertView.findViewById(R.id.txtTime);
        textView.setText(item.getPrice());

        return convertView;
    }
}