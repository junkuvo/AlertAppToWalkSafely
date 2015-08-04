package com.shipment_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tablet.R;
import com.shipment.ShipmentDetails;

public class DetailAdapter extends ArrayAdapter<String>{
	private final int rowResourceId;
    private final Context context;
    private final String[] Ids;
    
    private ShipmentDetails[] mDetails;
	
	public DetailAdapter(Context context, int resource,String[] objects, ShipmentDetails[] details) {
		super(context, resource,objects);
    	this.rowResourceId = resource;
    	this.context = context;
        this.Ids = objects;
        this.mDetails = details;
	}
	
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
   	
    	View rowView = null;
    	if (convertView == null) {
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		rowView = inflater.inflate(rowResourceId, parent, false);
    	}else{
    		rowView = convertView;
    	}

//		rowView.setBackgroundColor(context.getResources().getColor(R.color.lightyellow));

//    	if(position % 2 == 0){
//    		rowView.setBackgroundColor(context.getResources().getColor(R.color.lightyellow));
//    	}else{
//    		rowView.setBackgroundColor(Color.WHITE);
//    	}
    	
		// header---------------------------------
		((TextView)rowView.findViewById(R.id.textView2)).setText(R.string.expectedShipQTY);
		((TextView)rowView.findViewById(R.id.textView4)).setText(R.string.actualShipQTY);
    	
    	int id = Integer.parseInt(Ids[position]);
    	
    	if(id < Ids.length){
            ShipmentDetails di = mDetails[id];

    		TextView tv1 = (TextView) rowView.findViewById(R.id.textView5);
    		TextView tv = (TextView) rowView.findViewById(R.id.text1);
    		TextView tv2 = (TextView) rowView.findViewById(R.id.textView6);
    		TextView tv3 = (TextView) rowView.findViewById(R.id.textView7);
    		TextView tv4 = (TextView) rowView.findViewById(R.id.textView8);
    		tv1.setText(di.getShipmentLineNumber());
    		tv.setText(di.getSku());
    		tv2.setText(di.getDescription());
    		tv3.setText("：" + String.valueOf(di.getQtyExpected()));
    		tv4.setText("：" + String.valueOf(di.getQtyReceived()));

    		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView1);
    		int imageFileID = 0;    		
        	int status = Integer.parseInt(di.getStatus());
        	if(status <= 16){
        		imageFileID = R.drawable.icon_awaiting_small;    		
        	}else if(status <= 54 && status >= 17){
        		imageFileID = R.drawable.icon_checked_small;    		    		    		    		    		
        	}else if(status <= 67 && status >= 55){
        		imageFileID = R.drawable.icon_picking_small;    		    		    		    		
        	}else if(status <= 94 && status >= 68){
        		imageFileID = R.drawable.icon_packed_small;    		    		    		
        	}else{
        		imageFileID = R.drawable.icon_shipped_small;    		    		
        	}

    		imageView.setImageResource(imageFileID);
    	}

		return rowView;
    }	
}
