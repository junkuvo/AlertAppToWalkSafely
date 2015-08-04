package common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import com.example.tablet.R;

public class CalendarDialog extends DialogFragment {
	
	AlertDialog.Builder builder;
    private static CallBackTask mCallbacktask;
	
	private int mYear;
	private int mMonth;
	private int mDate;
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.calendar_title));
        builder.setMessage(getString(R.string.calendar_message));
        builder.setIcon(R.drawable.calendar );
        builder.setPositiveButton(getString(R.string.calendar_btnpositive), new DialogInterface.OnClickListener() {

        	@Override
        	public void onClick(DialogInterface dialog, int which) {
    			mCallbacktask.CallBack(mYear, mMonth, mDate);
    			mCallbacktask = null;

            }
        });
        builder.setNegativeButton(getString(R.string.calendar_btnnegative), new DialogInterface.OnClickListener() {

        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        	}
        });

        LayoutInflater inflaterMain = LayoutInflater.from(getActivity().getBaseContext());
        View calendar = inflaterMain.inflate(R.layout.calendar, null);
        DatePicker dp = (DatePicker) calendar.findViewById(R.id.datePicker1);
        dp.init(mYear, mMonth, mDate, new OnDateChangedListener() {
        	@Override
        	public void onDateChanged(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
        		mYear = year;
        		mMonth = monthOfYear;
        		mDate = dayOfMonth;
        	}
        });

        builder.setView(calendar);
        
        return builder.create();
    }
    
    public void setOnCallBack(CallBackTask _cbj) {
    	mCallbacktask = _cbj;
    }

    public static class CallBackTask {
    	public void CallBack(int year, int month, int day) {
    	}

    	public void CallProgress(Integer progress) {
    	}
    }

	public int getmYear() {
		return mYear;
	}

	public void setmYear(int mYear) {
		this.mYear = mYear;
	}

	public int getmMonth() {
		return mMonth;
	}

	public void setmMonth(int mMonth) {
		this.mMonth = mMonth;
	}

	public int getmDate() {
		return mDate;
	}

	public void setmDate(int mDate) {
		this.mDate = mDate;
	}
}
