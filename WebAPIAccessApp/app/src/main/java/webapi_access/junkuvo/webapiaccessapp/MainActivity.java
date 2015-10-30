package webapi_access.junkuvo.webapiaccessapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ProgressDialog mProgressDialog;
    private HttpRequestExecution mHttpRequestExecution;
    private HttpConnectionSetting mHttpConnectionSetting;

    private ListView mListView;
    private List<JSONObject> mItemList;
    private ListItemAdapter mListItemAdapter;

    JSONObject mJsonObject = null;
    JSONArray mJsonArray = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // WebAPIからデータを取得する間のダイアログ
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.progress_message));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mHttpRequestExecution.cancel(true);
                dialog.dismiss();
                mHttpRequestExecution = null;
            }
        });
        mHttpConnectionSetting = new HttpConnectionSetting(this);
        getJSONArrayFromWebAPI();

        mListView = (ListView) findViewById(R.id.listView);
        mItemList = new ArrayList<JSONObject>();
        mListItemAdapter = new ListItemAdapter(this, 0, mItemList);
    }

    public void getJSONArrayFromWebAPI() {
        mHttpRequestExecution = new HttpRequestExecution(new HttpRequestExecution.AsyncCallback() {
            public void preExecute() {
                mProgressDialog.show();
            }

            public void postExecute(String jsonData) {
                registerDataToList(jsonData);
                mProgressDialog.dismiss();
            }

            //. API バージョンによって呼ばれるタイミングが違うため利用しない
            public void cancel() {
            }
        });
        mHttpConnectionSetting.startHttpRequestSpaceData(mHttpRequestExecution);
    }

    public void registerDataToList(String jsonData){
        try{
            if (jsonData != null) {

                //  ★この部分をJSONデータの形式に合わせて編集する必要があります。(アダプターのgetViewの中身)
                try {
                    if(jsonData.trim().substring(0,1).equals("[")){// Array か Object かの判断
                        mJsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            mItemList.add(mJsonArray.getJSONObject(i));
                        }
                    }else{
                        mJsonObject = new JSONObject( jsonData );
                        mItemList.add(mJsonObject);
                    }

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("test",String.valueOf(position));
                        }
                    });

                }catch (JSONException ex){
                    ex.printStackTrace();
                }

                mListView.setAdapter(mListItemAdapter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
