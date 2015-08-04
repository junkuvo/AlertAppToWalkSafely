package com.receipt_main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.tablet.R;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private Callbacks mCallbacks = sReceiptCallbacks;
    private View mView;

	private int mActivatedPosition = ListView.INVALID_POSITION;
	public static ReceiptAdapter sReceiptAdapter;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sReceiptCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		AsyncHttpRequest asyncTasc = new AsyncHttpRequest();
//		asyncTasc.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
//		    @Override
//		    public void CallBack(String Result) {
//		    	String[] ids = new String[ItemListActivity.sReceiptList.receipts.length];
//				for (int i= 0; i < ids.length; i++){
//					ids[i] = ItemListActivity.sReceiptList.receipts[i].getExternReceiptKey();//Integer.toString(i);
//				}
//				sReceiptAdapter = new ReceiptAdapter(getActivity(), R.layout.list_item,ids);
//				setListAdapter(sReceiptAdapter);
//		    };
//		    @Override
//		    public void CallProgress(Integer progress) {
//
//		    }
//		});
//		getReceiptsList();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	public static void getReceiptsList(){
		
		String mURL = "192.168.1.223:11010";
		String mPath = "InforService.svc/json/GetReceipt";
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("http");
		builder.encodedAuthority(mURL);
		builder.path(mPath);
		builder.appendQueryParameter("storer", "KRC");
		builder.appendQueryParameter("receiptdate", "20120706");
		builder.appendQueryParameter("id", "id");
		builder.appendQueryParameter("key", "key");

		AsyncHttpRequest bgHttpTask = new AsyncHttpRequest();
		bgHttpTask.execute(builder);
	}	

	
	
	public void setReceiptsList(ReceiptAdapter ra){
		setListAdapter(ra);
//	MusicalList.adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sReceiptCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,long id) {
		super.onListItemClick(listView, view, position, id);
        if(mView != null){
            mView.setBackgroundColor(Color.TRANSPARENT);
        }
        mView = view;
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
//		mCallbacks.onItemSelected(String.valueOf(position + 1));
		TextView tv = (TextView)view.findViewById(R.id.text1); 
//		Log.d("test",(String) tv.getText());
		mCallbacks.onItemSelected((String) tv.getText());
        view.setBackgroundColor(this.getResources().getColor(R.color.white));

		SearchView sv = (SearchView) getActivity().findViewById(R.id.searchView1);
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(sv.getWindowToken(),0);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	public void setUpdatedItemList(Context ct){
		// for calendar
		String[] ids = new String[ItemListActivity.sReceiptList.receipts.length];
		for (int i= 0; i < ids.length; i++){
			ids[i] = ItemListActivity.sReceiptList.receipts[i].getExternReceiptKey();//Integer.toString(i);
		}
		ReceiptAdapter.removeReceiptItems();
		sReceiptAdapter = null;
		sReceiptAdapter = new ReceiptAdapter(ct, R.layout.list_item,ids);
		ReceiptAdapter.convertReceiptToArray(ItemListActivity.sReceiptList.receipts);
		
	}
}
