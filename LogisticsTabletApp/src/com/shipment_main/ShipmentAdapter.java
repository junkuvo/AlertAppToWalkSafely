package com.shipment_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tablet.R;
import com.shipment.Shipments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShipmentAdapter extends ArrayAdapter<String>{

	private int rowResourceId;
    private Context context;
    private String[] Ids;
	
	public ShipmentAdapter(Context context, int resource, String[] objects) {
		super(context, resource,objects);
    	this.rowResourceId = resource;
    	this.context = context;
//        this.Ids = objects;
	}
	
	public static ArrayList<Shipments> Shipment_Items = new ArrayList<Shipments>();
	public static Map<String, Shipments> ShipmentItems_MAP = new HashMap<String, Shipments>();
	
	// called prior to constructor
	static {
		convertShipmentToArray(ItemListActivity.sShipmentList.shipments);
	}
	
	static public void removeShipmentItems(){
		Shipment_Items.clear();
		ShipmentItems_MAP.clear();
//		int size = Shipment_Items.size();
//		for(int i = 0 ; i < size ; i++ ){
//			ShipmentItems_MAP.remove(Shipment_Items.get(size - i - 1).getExternShipmentKey());
//			Shipment_Items.remove(size - i - 1);
//		}
	}

	public static void addItem(Shipments item, String itemId) {
		Shipment_Items.add(item);
		ShipmentItems_MAP.put(itemId, item);
	}
	
	public static void convertShipmentToArray(Shipments[] items) {
		for(int i = 0 ; i < items.length ; i++){
//			ShipmentAdapter.addItem(items[i],String.valueOf(i + 1));
			ShipmentAdapter.addItem(items[i], items[i].getExternShipmentKey());
		}
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

//    	if(position % 2 == 0){
//    		rowView.setBackgroundColor(context.getResources().getColor(R.color.lightyellow));
//    	}else{
//    		rowView.setBackgroundColor(Color.WHITE);
//    	}

    	//    	try{

//        String data = getItem(position); -- ★★★ShipmentAdapterのobjectが取得できる(フィルタ後のposition)

    	Shipments di = ShipmentItems_MAP.get(getItem(position));
//    	Shipments di = Shipment_Items.get(getItem(position));// ★★★Shipment_Itemsはint でしか取得できない
    	
		// header---------------------------------
		((TextView)rowView.findViewById(R.id.textView1)).setText(R.string.expectedShipDate);
		((TextView)rowView.findViewById(R.id.textView2)).setText(R.string.expectedShipQTY);
		((TextView)rowView.findViewById(R.id.textView3)).setText(R.string.actualShipDate);
		((TextView)rowView.findViewById(R.id.textView4)).setText(R.string.actualShipQTY);


    	
    	TextView tv0 = (TextView) rowView.findViewById(R.id.text1);
    	TextView tv = (TextView) rowView.findViewById(R.id.textView5);
    	TextView tv2 = (TextView) rowView.findViewById(R.id.textView6);
    	TextView tv3 = (TextView) rowView.findViewById(R.id.textView7);
    	TextView tv4 = (TextView) rowView.findViewById(R.id.textView8);
    	tv0.setText(di.getExternShipmentKey());
    	if(di.getExpectedShipmentDate() != null && !di.getExpectedShipmentDate().equals("")){
    		tv.setText("：" + di.getExpectedShipmentDate().substring(0, 10));
    	}else{
    		tv.setText("：");
    	}
    	if(di.getShipmentDate() != null && !di.getShipmentDate().equals("")){
    		tv2.setText("：" + di.getShipmentDate().substring(0, 10));
    	}else{
    		tv2.setText("：");
    	}
    	tv3.setText("：" + di.getQtyExpected());
    	tv4.setText("：" + di.getQtyReceived());

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
    	//    	}catch(Exception ex){
    	//    	}
		return rowView;
    }
  
	
}
