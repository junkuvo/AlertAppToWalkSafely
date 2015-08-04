package com.receipt_main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tablet.R;
import com.receipt.Receipts;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
	public static final String ARG_ITEM_ID = "item_id";// just a key name of item in main list

	private Receipts mReceiptItem;

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
			mReceiptItem = ReceiptAdapter.ReceiptItems_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,container, false);
        rootView.setBackgroundColor(getActivity().getResources().getColor(R.color.white));

		if (mReceiptItem != null) {
			((TextView) rootView.findViewById(R.id.item_detail)).setText(mReceiptItem.getExternReceiptKey());
			((TextView) rootView.findViewById(R.id.supplierValue)).setText(mReceiptItem.getSupplierName());
			if(mReceiptItem.getExpectedReceiptDate() != null && !mReceiptItem.getExpectedReceiptDate().equals("")){
				((TextView) rootView.findViewById(R.id.ExpReceiptDateValue)).setText("：" + mReceiptItem.getExpectedReceiptDate().substring(0, 10));
			}else{
				((TextView) rootView.findViewById(R.id.ExpReceiptDateValue)).setText("");
			}

			if(mReceiptItem.getReceiptDate() != null && !mReceiptItem.getReceiptDate().equals("")){
				((TextView) rootView.findViewById(R.id.ReceiptDateValue)).setText("：" + mReceiptItem.getReceiptDate().substring(0, 10));
			}else{
				((TextView) rootView.findViewById(R.id.ReceiptDateValue)).setText("");
			}
			((TextView) rootView.findViewById(R.id.ExpReceiptQTYValue)).setText("：" + String.valueOf(mReceiptItem.getQtyExpected()));
			((TextView) rootView.findViewById(R.id.ReceiptQTYValue)).setText("：" + String.valueOf(mReceiptItem.getQtyReceived()));

			ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView1);
    		int imageFileID = 0;
    		switch (Integer.parseInt(mReceiptItem.getStatus())){
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
    		
    		ListView listView = (ListView) rootView.findViewById(R.id.listView1);

	    	String[] ids = new String[mReceiptItem.details.length];
			for (int i= 0; i < ids.length; i++){
				ids[i] = Integer.toString(i);
			}
			DetailAdapter da = new DetailAdapter(getActivity(), R.layout.list_item_detail,ids, mReceiptItem.details);
			listView.setAdapter(da);
			Log.d("test","test3");
		}

		return rootView;
	}
}
