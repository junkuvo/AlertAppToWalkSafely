package com.shipment_main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tablet.R;
import com.shipment.Shipments;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
	public static final String ARG_ITEM_ID = "item_id";// just a key name of item in main list

	private Shipments mShipmentItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey(ARG_ITEM_ID)) {
//			Log.d("test",getArguments().getString(ARG_ITEM_ID));
			mShipmentItem = ShipmentAdapter.ShipmentItems_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_shipment_item_detail,container, false);
    	rootView.setBackgroundColor(getActivity().getResources().getColor(R.color.white));

		if (mShipmentItem != null) {
			((TextView) rootView.findViewById(R.id.item_detail)).setText(mShipmentItem.getExternShipmentKey());
			((TextView) rootView.findViewById(R.id.supplierValue)).setText(mShipmentItem.getSupplierName());
			if(mShipmentItem.getExpectedShipmentDate() != null && !mShipmentItem.getExpectedShipmentDate().equals("")){
				((TextView) rootView.findViewById(R.id.ExpShipmentDateValue)).setText("：" + mShipmentItem.getExpectedShipmentDate().substring(0, 10));
			}else{
				((TextView) rootView.findViewById(R.id.ExpShipmentDateValue)).setText("");
			}

			if(mShipmentItem.getShipmentDate() != null && !mShipmentItem.getShipmentDate().equals("")){
				((TextView) rootView.findViewById(R.id.ShipmentDateValue)).setText("：" + mShipmentItem.getShipmentDate().substring(0, 10));
			}else{
				((TextView) rootView.findViewById(R.id.ShipmentDateValue)).setText("");
			}
			((TextView) rootView.findViewById(R.id.ExpShipmentQTYValue)).setText("：" + String.valueOf(mShipmentItem.getQtyExpected()));
			((TextView) rootView.findViewById(R.id.ShipmentQTYValue)).setText("：" + String.valueOf(mShipmentItem.getQtyReceived()));

			ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView1);
    		int imageFileID = 0;

    		int status = Integer.parseInt(mShipmentItem.getStatus());
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
    		
    		ListView listView = (ListView) rootView.findViewById(R.id.listView1);

	    	String[] ids = new String[mShipmentItem.details.length];
			for (int i= 0; i < ids.length; i++){
				ids[i] = Integer.toString(i);
			}
			DetailAdapter da = new DetailAdapter(getActivity(), R.layout.list_item_detail,ids, mShipmentItem.details);
			listView.setAdapter(da);

		}

		return rootView;
	}
}
