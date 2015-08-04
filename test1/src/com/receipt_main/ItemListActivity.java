package com.receipt_main;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tablet.R;
import com.receipt.ReceiptsList;

import common.CalendarDialog;


/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements ItemListFragment.Callbacks,SearchView.OnQueryTextListener,OnRefreshListener  {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	static public ReceiptsList sReceiptList;
	static public int sYear;
	static public int sMonth;
	static public int sDate;
	
		
	public String[] actions = new String[] {
			"Site-Storer-Wh1",
			"Site-Storer-Wh2",
			"Site-Storer-Wh3",
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);
		getActionBar().setIcon(getResources().getDrawable(R.drawable.receiving_gray1));
		getActionBar().setTitle("ABC社　入荷情報");
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        		Toast.makeText(getBaseContext(), "You selected : " + actions[itemPosition], Toast.LENGTH_SHORT).show();
     	
            	AsyncHttpRequest asyncTasc = new AsyncHttpRequest();
            	asyncTasc.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
            		@Override
            		public void CallBack(String Result) {
            			
        		    	String[] ids = new String[ItemListActivity.sReceiptList.receipts.length];
        				for (int i= 0; i < ids.length; i++){
        					ids[i] = ItemListActivity.sReceiptList.receipts[i].getExternReceiptKey();//Integer.toString(i);
        				}
        				ItemListFragment.sReceiptAdapter = new ReceiptAdapter(getBaseContext(), R.layout.list_item,ids);
            			

            			ItemListFragment ilf = new ItemListFragment();
            			ilf.setUpdatedItemList(getBaseContext());
            			((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setListAdapter(ItemListFragment.sReceiptAdapter);
            			//            				View test = findViewById(R.id.item_list);
            			//            				test.invalidate();
            		};

            		@Override
            		public void CallProgress(Integer progress) {

            		}
            	});

            	ItemListFragment.getReceiptsList();
                
                hideSoftKeyboard();
                return false;
            }
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.spinner_item, actions);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
        getActionBar().setListNavigationCallbacks(adapter, navigationListener);
 
		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setActivateOnItemClick(true);
		}

		sReceiptList = new ReceiptsList();
		
		SearchView searchView = (SearchView) findViewById(R.id.searchView1);
		searchView.setOnQueryTextListener(this);
		
		Button ibtn = (Button) findViewById(R.id.imageButton1);
		ibtn.setOnClickListener(mImageButtonClickListener);
		ibtn = (Button) findViewById(R.id.imageButton2);
		ibtn.setOnClickListener(mImageButtonClickListener);
		ibtn = (Button) findViewById(R.id.imageButton3);
		ibtn.setOnClickListener(mImageButtonClickListener);
		ibtn = (Button) findViewById(R.id.imageButton4);
		ibtn.setOnClickListener(mImageButtonClickListener);
		ibtn = (Button) findViewById(R.id.imageButton5);
		ibtn.setOnClickListener(mImageButtonClickListener);
		
		SwipeRefreshLayout sw = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_widget);
		sw.setOnRefreshListener(this);
		
		((Button) findViewById(R.id.imageButton1)).setBackgroundColor(getResources().getColor(R.color.gray));
		((Button) findViewById(R.id.imageButton2)).setBackgroundColor(getResources().getColor(R.color.gray));
		((Button) findViewById(R.id.imageButton3)).setBackgroundColor(getResources().getColor(R.color.gray));
        ((Button) findViewById(R.id.imageButton4)).setBackgroundColor(getResources().getColor(R.color.lightgreen));
		((Button) findViewById(R.id.imageButton5)).setBackgroundColor(getResources().getColor(R.color.gray));

	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		ItemListFragment.sReceiptAdapter.getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_calendar, menu);

		MenuItem mi = menu.getItem(3);
		final TextView tvCalendar = (TextView)mi.getActionView();
		final Time time = new Time();
		time.setToNow();
		sYear = time.year;
		sMonth = time.month + 1;
		sDate = time.monthDay;
//		String stime = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日";
		String stime = time.year + "/" + (time.month+1) + "/" + time.monthDay + "";
		tvCalendar.setText(stime);
		tvCalendar.setPadding(100, 0, 0, 0);
		
		mi = menu.getItem(4);
		ImageButton ibtn = (ImageButton)mi.getActionView();
		Resources r = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.calendar);
		ibtn.setImageBitmap(bmp);
		//	    ibtn.setBackgroundColor(Color.TRANSPARENT);
		ibtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_click_style));
		ibtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Calendar ", Toast.LENGTH_SHORT).show();
				CalendarDialog dialog = new CalendarDialog();
                dialog.setOnCallBack(new CalendarDialog.CallBackTask() {
					@Override
					public void CallBack(int year, int month, int day) {
						sYear = year;
						sMonth = month + 1;
						sDate = day;
//						tvCalendar.setText(sYear + "年" + sMonth + "月" + sDate + "日");
						tvCalendar.setText(sYear + "/" + sMonth + "/" + sDate + "");
						
		            	AsyncHttpRequest asyncTasc = new AsyncHttpRequest();
		            	asyncTasc.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
		            		@Override
		            		public void CallBack(String Result) {
		            			ItemListFragment ilf = new ItemListFragment();
		            			ilf.setUpdatedItemList(getBaseContext());
		            			((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setListAdapter(ItemListFragment.sReceiptAdapter);
		            		};

		            		@Override
		            		public void CallProgress(Integer progress) {

		            		}
		            	});

		            	ItemListFragment.getReceiptsList();
						
					};
				});

				dialog.setmYear(sYear);
				dialog.setmMonth(sMonth - 1);
				dialog.setmDate(sDate);
				dialog.show(getFragmentManager(), "dialog");
			}
		});
		
		
		mi = menu.getItem(0);
		Button btn = (Button)mi.getActionView();
//		btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_click_style));
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.setOnClickListener(mClickListener);
		btn.setText(getString(R.string.before_receipt));
        btn.setWidth(getResources().getInteger(R.integer.tab_btn_width));
		mi = menu.getItem(1);
		btn = (Button)mi.getActionView();
//		btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_click_style));
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.setOnClickListener(mClickListener);
		btn.setText(getString(R.string.after_receipt));
        btn.setWidth(getResources().getInteger(R.integer.tab_btn_width));
		mi = menu.getItem(2);
		btn = (Button)mi.getActionView();
//		btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_click_style));
		btn.setOnClickListener(mClickListener);
		btn.setText(getString(R.string.all));
        btn.setWidth(getResources().getInteger(R.integer.tab_btn_width));
//		btn.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		btn.setBackgroundColor(Color.GRAY);
		
		return true;
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.mainlayout);
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(ll.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        ll.requestFocus();
 
        return false;
    }

	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
//        	v.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
            Toast.makeText(getBaseContext(),v.toString(),Toast.LENGTH_SHORT).show();
			v.setBackgroundColor(Color.GRAY);
        	Button btn;
			switch(v.getId()){
			case R.id.filter_before:
				btn = (Button) findViewById(R.id.filter_after);
				btn.setBackgroundColor(Color.TRANSPARENT);
				btn = (Button) findViewById(R.id.filter_all);
				btn.setBackgroundColor(Color.TRANSPARENT);
				break;
			case R.id.filter_after:
				btn = (Button) findViewById(R.id.filter_before);
				btn.setBackgroundColor(Color.TRANSPARENT);
				btn = (Button) findViewById(R.id.filter_all);
				btn.setBackgroundColor(Color.TRANSPARENT);				
				break;
			case R.id.filter_all:
				btn = (Button) findViewById(R.id.filter_before);
				btn.setBackgroundColor(Color.TRANSPARENT);
				btn = (Button) findViewById(R.id.filter_after);
				btn.setBackgroundColor(Color.TRANSPARENT);
				break;
			}
			hideSoftKeyboard();
		}
	};
	
	
	private OnClickListener mImageButtonClickListener = new OnClickListener() {
		
		@SuppressLint("ResourceAsColor")
		@Override
		public void onClick(View v) {
//			v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			Button btn;
			switch(v.getId()){
			case R.id.imageButton5:
				((Button) findViewById(R.id.imageButton1)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton2)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton3)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton4)).setBackgroundColor(getResources().getColor(R.color.gray));
				Toast.makeText(getBaseContext(), "You typed : dash", Toast.LENGTH_SHORT).show();
				break;
			case R.id.imageButton4:
				((Button) findViewById(R.id.imageButton1)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton2)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton3)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton5)).setBackgroundColor(getResources().getColor(R.color.gray));
		        Toast.makeText(getBaseContext(), "You typed : receive", Toast.LENGTH_SHORT).show();
				break;
			case R.id.imageButton3:
				((Button) findViewById(R.id.imageButton1)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton2)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton5)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton4)).setBackgroundColor(getResources().getColor(R.color.gray));
                v.setBackgroundColor(getResources().getColor(R.color.yellow));
                Toast.makeText(getBaseContext(), "You typed : ship", Toast.LENGTH_SHORT).show();
				Intent detailIntent = new Intent(ItemListActivity.this, com.shipment_main.ItemListActivity.class);
				startActivity(detailIntent);
				overridePendingTransition(0, 0);
		        
				break;
			case R.id.imageButton2:
				((Button) findViewById(R.id.imageButton1)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton5)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton3)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton4)).setBackgroundColor(getResources().getColor(R.color.gray));
		        Toast.makeText(getBaseContext(), "You typed : inventry", Toast.LENGTH_SHORT).show();
				break;
			case R.id.imageButton1:
				((Button) findViewById(R.id.imageButton2)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton5)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton3)).setBackgroundColor(getResources().getColor(R.color.gray));
				((Button) findViewById(R.id.imageButton4)).setBackgroundColor(getResources().getColor(R.color.gray));
		        Toast.makeText(getBaseContext(), "You typed : setting", Toast.LENGTH_SHORT).show();
				break;
			}
			hideSoftKeyboard();
		}
	};

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {

			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
//			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, ItemListActivity.sReceiptList.receipts[Integer.parseInt(id) - 1].getExternReceiptKey());
//			Log.d("test",ItemListActivity.sReceiptList.receipts[Integer.parseInt(id) - 1].getExternReceiptKey());

			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
//			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, ItemListActivity.sReceiptList.receipts[Integer.parseInt(id) - 1].getExternReceiptKey());
			startActivity(detailIntent);
		}
	}
	
	public void hideSoftKeyboard(){
		SearchView sv = (SearchView) findViewById(R.id.searchView1);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(sv.getWindowToken(),0);
	}

	@Override
	public void onRefresh() {
		Toast.makeText(getBaseContext(), "update", Toast.LENGTH_SHORT).show();
    	AsyncHttpRequest asyncTasc = new AsyncHttpRequest();
    	asyncTasc.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
    		@Override
    		public void CallBack(String Result) {
    			ItemListFragment ilf = new ItemListFragment();
    			ilf.setUpdatedItemList(getBaseContext());
    			((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setListAdapter(ItemListFragment.sReceiptAdapter);
    		};

    		@Override
    		public void CallProgress(Integer progress) {

    		}
    	});
    	ItemListFragment.getReceiptsList();
    	
	}
}
