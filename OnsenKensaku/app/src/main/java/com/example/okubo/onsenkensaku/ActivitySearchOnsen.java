package com.example.okubo.onsenkensaku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivitySearchOnsen extends Activity {

    private Utils mUtils;
    private HttpConnectionSetting mHttpConnectionSetting;
    private CurrentLocationFinder mCurrentLocationFinder;
    public  static OnsenData[] sOnsens;
    private ProgressDialog mProgressDialog;
    private HttpRequestExecution mHttpRequestExecution;


    private ListView mListView;
    private List<OnsenData> mItemList;
    private ListItemAdapter mListItemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.progress_message));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Cancel),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                mHttpRequestExecution.cancel(true);
                dialog.dismiss();
                mHttpRequestExecution = null;
            }
        });

        mUtils = new Utils();
        mCurrentLocationFinder = new CurrentLocationFinder(this);
        mHttpConnectionSetting = new HttpConnectionSetting(this);
        Button btnPreSearch = (Button) findViewById(R.id.btnPreSearch);
        btnPreSearch.setOnClickListener(mClickListener);
        Button btnLocSearch = (Button) findViewById(R.id.btnLocSearch);
        btnLocSearch.setOnClickListener(mClickListenerLocation);

        if(mUtils.checkNetworkStatus(this)){
        }else{
            msgNetworkError();
            return;
        }

        if(((TextView) findViewById(R.id.lblPreState)).getVisibility() != View.INVISIBLE) {
            setPrefectureData();
        }else{
            setSelectedItem();
        }

        if (mCurrentLocationFinder.hasCurrentLocationData()) {
            changeLocationButtonState();
        }else{
            if (mCurrentLocationFinder.checkGPSSetting()) {
                mCurrentLocationFinder.getCurrentLocation();
                waitMyLocation();
            } else {
                msgGPSSetting();
            }
        }

        mListView = (ListView) findViewById(R.id.listView_home);
        mItemList = new ArrayList<OnsenData>();
        mListItemAdapter = new ListItemAdapter(this, 0, mItemList);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCurrentLocationFinder != null) {
            mCurrentLocationFinder.removeLocationListener();
        }
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferenceName),MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.preferenceKeyLatitude));
        editor.remove(getString(R.string.preferenceKeyLongitude));
        editor.commit();
    }

    public void setPrefectureData(){
        HttpRequestPrefectureList asyncTask = new HttpRequestPrefectureList();
        asyncTask.setOnCallBack(new HttpRequestPrefectureList.CallBackTask() {
            @Override
            public void CallBack(String Result) {
                if (Result != null) {
                    ((TextView) findViewById(R.id.lblPreState)).setVisibility(View.INVISIBLE);
                    ((Button) findViewById(R.id.btnPreSearch)).setEnabled(true);

                    getPrefectures(Result);
                } else {
                }
            }

            @Override
            public void CallProgress(Integer progress) {
            }
        });
        mHttpConnectionSetting.startHttpRequestPrefectureList(asyncTask);
    }

    public void setSelectedItem(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
        long index = preferences.getLong("listIndex", 0);
        Spinner spinner = (Spinner) findViewById(R.id.ddrPreSearch);
        spinner.setSelection((int) index);
    }

    public void getPrefectures(String prefectures){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String[] prefecture = prefectures.split(",");
        int i = 0;
        for(i = 0;i < prefecture.length; i++){
            adapter.add(mUtils.convertToString(prefecture[i].split(":")[1]).replace("\"","").replace("}",""));
        }

        Spinner spinner = (Spinner) findViewById(R.id.ddrPreSearch);
        spinner.setAdapter(adapter);
    }


    public void setOnsenDataPrefecture(String prefectureId){
        mHttpRequestExecution = new HttpRequestExecution(new HttpRequestExecution.AsyncCallback() {
            public void preExecute() {
                mProgressDialog.show();
            }

            public void postExecute(JSONObject Result) {
                if (Result != null) {
                    OnsenData[] onsenDatas = parseJSONToOnsenDatas(Result);

                    for(int i = 0;i < onsenDatas.length;i++) {
                        mItemList.add(onsenDatas[i]);
                    }

                    sOnsens = onsenDatas;
                    Intent intent = new Intent(ActivitySearchOnsen.this, ActivityShowOnsenLocationOnMap.class);
                    double[] position = getCameraCenter(onsenDatas);
                    intent.putExtra("latitude",position[0]);
                    intent.putExtra("longitude",position[1]);
                    intent.putExtra("method","prefecture");

                    SharedPreferences preferences = getSharedPreferences(getString(R.string.preferenceName), MODE_PRIVATE);
                    Spinner spinner = (Spinner) findViewById(R.id.ddrPreSearch);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong("listIndex", spinner.getSelectedItemPosition());
                    editor.commit();
                    mProgressDialog.dismiss();

                    mListView.setAdapter(mListItemAdapter);
//                    mListView.setSelectionFromTop(sListPosition,sY);

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getApplicationContext(),"click", Toast.LENGTH_SHORT);
                        }
                    });

//                    startActivity(intent);
                }
            }

            public void cancel() {
            }
        });

        mHttpConnectionSetting.startHttpRequestOnsenInforByPrefecture(mHttpRequestExecution, prefectureId);
    }

    public void setOnsenDataCurrentLocation(final double latitude, final double longitude){
        mHttpRequestExecution = new HttpRequestExecution(new HttpRequestExecution.AsyncCallback() {
            public void preExecute() {
                mProgressDialog.show();
            }

            public void postExecute(JSONObject Result) {
                if (Result != null) {
                    try {
                        OnsenData[] onsenDatas = parseJSONToOnsenDatas(Result);

                        sOnsens = onsenDatas;
                        Intent intent = new Intent(ActivitySearchOnsen.this, ActivityShowOnsenLocationOnMap.class);
                        intent.putExtra("latitude",latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("method", "location");

                        mProgressDialog.dismiss();

                        startActivity(intent);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            public void cancel() {
            }
        });

        mHttpConnectionSetting.startHttpRequestOnsenInforByLocation(mHttpRequestExecution, latitude, longitude);
    }

    public OnsenData[] parseJSONToOnsenDatas(JSONObject jsonObject){
        try {
            JSONArray eventArray = jsonObject.getJSONArray("events");
            OnsenData[] onsenDatas = new OnsenData[eventArray.length()];
            for (int i = 0; i < eventArray.length(); i++) {
                onsenDatas[i] = new OnsenData();
                JSONObject eventObj = eventArray.getJSONObject(i);
                JSONObject event = eventObj.getJSONObject("Onsen");
                onsenDatas[i].setId(event.getInt("id"));
                onsenDatas[i].setName(event.getString("name"));
                onsenDatas[i].setKana(event.getString("kana"));
                onsenDatas[i].setAddress(event.getString("address"));
                onsenDatas[i].setTel(event.getString("tel"));
                onsenDatas[i].setPrice(event.getString("price"));
                onsenDatas[i].setClose_day(event.getString("close_day"));
                onsenDatas[i].setOpen_hour(event.getString("open_hour"));
                onsenDatas[i].setSpring_quality(event.getString("spring_quality"));
                onsenDatas[i].setLatitude(event.getDouble("latitude"));
                onsenDatas[i].setLongitude(event.getDouble("longitude"));
            }
            return onsenDatas;
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public void waitMyLocation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mCurrentLocationFinder.isLocationSearching()) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blinkSearchingMessage();
                            }
                        });
                        android.os.SystemClock.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                mCurrentLocationFinder.initializeLocationSearching();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeLocationButtonState();                    }
                });
            }
        }).start();
    }

    public void msgGPSSetting(){
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(this.getString(R.string.DialogTitle_GPSSetting))
                .setMessage(this.getString(R.string.DialogMessage_GPSSetting))
                .setPositiveButton(this.getString(R.string.OK),new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        ((TextView) findViewById(R.id.lblLocSearch)).setText(getString(R.string.locationSearchProcessing));
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                }).setNegativeButton(this.getString(R.string.Cancel),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
//                mMyLocation.getCurrentLocation();
//                waitMyLocation();
                ((TextView) findViewById(R.id.lblLocSearch)).setText(getString(R.string.DialogMessage_GPSError));
                dialog.cancel();
            }
        }).show();
    }

    public void msgNetworkError(){
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(this.getString(R.string.DialogTitle_NetworkError))
                .setMessage(this.getString(R.string.DialogTitle_NetworkErrorMessage))
                .setPositiveButton(this.getString(R.string.OK),new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        finish();
                    }
                }).show();
    }

    public void blinkSearchingMessage(){
        String message = ((TextView) findViewById(R.id.lblLocSearch)).getText().toString();
        message = message.replace("...","") + ".";
        ((TextView) findViewById(R.id.lblLocSearch)).setText(message);
    }

    public void changeLocationButtonState(){
        ((TextView) findViewById(R.id.lblLocSearch)).setText(getString(R.string.locationSearchState));
        ((Button) findViewById(R.id.btnLocSearch)).setEnabled(true);
    }

    public double[] getCameraCenter(OnsenData[] onsenData){
        int i;
        double[] position = new double[2];

        for(i = 0;i < onsenData.length;i++){
            position[0] += onsenData[i].getLatitude();
            position[1] += onsenData[i].getLongitude();
        }
        position[0] = position[0]/i;
        position[1] = position[1]/i;
        return position;
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner) findViewById(R.id.ddrPreSearch);
            setOnsenDataPrefecture(String.valueOf(spinner.getSelectedItemId() + 1));
        }
    };

    private View.OnClickListener mClickListenerLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCurrentLocationFinder.hasCurrentLocationData()) {
                setOnsenDataCurrentLocation(mCurrentLocationFinder.getCurrentLatitude(), mCurrentLocationFinder.getCurrentLongitude());
            }
        }
    };

}
