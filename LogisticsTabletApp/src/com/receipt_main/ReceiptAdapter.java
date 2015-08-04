package com.receipt_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tablet.R;
import com.receipt.Receipts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiptAdapter  extends ArrayAdapter<String>{

	private int rowResourceId;
    private Context context;
    private String[] Ids;
	
	public ReceiptAdapter(Context context, int resource,String[] objects) {
		super(context, resource,objects);
    	this.rowResourceId = resource;
    	this.context = context;
//        this.Ids = objects;
	}
	
	public static ArrayList<Receipts> Receipt_Items = new ArrayList<Receipts>();
	public static Map<String, Receipts> ReceiptItems_MAP = new HashMap<String, Receipts>();
	
	// called prior to constructor
	static {
		convertReceiptToArray(ItemListActivity.sReceiptList.receipts);
	}
	
	static public void removeReceiptItems(){
		Receipt_Items.clear();
		ReceiptItems_MAP.clear();
//		int size = Receipt_Items.size();
//		for(int i = 0 ; i < size ; i++ ){
//			ReceiptItems_MAP.remove(Receipt_Items.get(size - i - 1).getExternReceiptKey());
//			Receipt_Items.remove(size - i - 1);
//		}
	}

	public static void addItem(Receipts item, String itemId) {
		Receipt_Items.add(item);
		ReceiptItems_MAP.put(itemId, item);
	}
	
	public static void convertReceiptToArray(Receipts[] items) {
		for(int i = 0 ; i < items.length ; i++){
//			ReceiptAdapter.addItem(items[i],String.valueOf(i + 1));
			ReceiptAdapter.addItem(items[i],items[i].getExternReceiptKey());
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
//    		rowView.setBackgroundColor(context.getResources().getColor(R.color.aliceblue));
//    	}else{
//    		rowView.setBackgroundColor(Color.WHITE);
//    	}

    	Receipts di = ReceiptItems_MAP.get(getItem(position));
//    	Receipts di = Receipt_Items.get(getItem(position));// ★★★Receipt_Itemsはint でしか取得できない
    	
    	TextView tv0 = (TextView) rowView.findViewById(R.id.text1);
    	TextView tv = (TextView) rowView.findViewById(R.id.textView5);
    	TextView tv2 = (TextView) rowView.findViewById(R.id.textView6);
    	TextView tv3 = (TextView) rowView.findViewById(R.id.textView7);
    	TextView tv4 = (TextView) rowView.findViewById(R.id.textView8);
    	tv0.setText(di.getExternReceiptKey());
    	if(di.getExpectedReceiptDate() != null && !di.getExpectedReceiptDate().equals("")){
    		tv.setText("：" + di.getExpectedReceiptDate().substring(0, 10));
    	}else{
    		tv.setText("：");
    	}
    	if(di.getReceiptDate() != null && !di.getReceiptDate().equals("")){
    		tv2.setText("：" + di.getReceiptDate().substring(0, 10));
    	}else{
    		tv2.setText("：");
    	}
    	tv3.setText("：" + di.getQtyExpected());
    	tv4.setText("：" + di.getQtyReceived());

    	ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView1);
    	int imageFileID = 0;

    	switch (Integer.parseInt(di.getStatus())){
    	case 0:
    		imageFileID = R.drawable.icon_awaiting2;
    		break;
    	case 5:
    		imageFileID = R.drawable.icon_receiving;
    		break;
    	case 9:
    		imageFileID = R.drawable.icon_instock;
    		break;
    	}
    	imageView.setImageResource(imageFileID);
    	//    	}catch(Exception ex){
    	//    	}
		return rowView;
    }
  
	
}
