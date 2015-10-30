package webapi_access.junkuvo.webapiaccessapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import org.json.JSONArray;

public class MainActivity extends Activity {

    private ProgressDialog mProgressDialog;
    private HttpRequestExecution mHttpRequestExecution;
    private HttpConnectionSetting mHttpConnectionSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

    }


    public void getJSONArrayFromWebAPI() {
        mHttpRequestExecution = new HttpRequestExecution(new HttpRequestExecution.AsyncCallback() {
            public void preExecute() {
                mProgressDialog.show();
            }

            public void postExecute(JSONArray Result) {
                if (Result != null) {

                    mProgressDialog.dismiss();
                }
            }

            public void cancel() {
            }
        });
        mHttpConnectionSetting.startHttpRequestSpaceData(mHttpRequestExecution);

    }
}
