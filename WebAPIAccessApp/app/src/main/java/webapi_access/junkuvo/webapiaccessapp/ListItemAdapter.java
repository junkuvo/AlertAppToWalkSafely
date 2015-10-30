package webapi_access.junkuvo.webapiaccessapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class ListItemAdapter extends ArrayAdapter<JSONObject> {
    private LayoutInflater layoutInflater_;
    private Activity mActivity;

    public ListItemAdapter(Context context, int textViewResourceId, List<JSONObject> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActivity = (Activity)context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            JSONObject item = (JSONObject) getItem(position);

            if (null == convertView) {
                convertView = layoutInflater_.inflate(R.layout.list_item_layout, null);
            }

            TextView textView;
            textView = (TextView) convertView.findViewById(R.id.txtTime);
            textView.setText(String.valueOf(item.getInt("id")));

//            textView = (TextView) convertView.findViewById(R.id.txtDate);
//            JSONObject jsonObjectOwner = item.getJSONObject("owner");
//            textView.setText(String.valueOf(jsonObjectOwner.getString("login")));

        }catch (JSONException ex){
            ex.printStackTrace();
        }

        return convertView;
    }
}