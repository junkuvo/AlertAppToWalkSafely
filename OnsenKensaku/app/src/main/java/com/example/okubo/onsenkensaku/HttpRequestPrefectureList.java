package com.example.okubo.onsenkensaku;

import android.net.Uri.Builder;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpRequestPrefectureList extends AsyncTask<Builder, Void, String> {

    private String accessResult = "";
    private static CallBackTask mCallbacktask;

    public HttpRequestPrefectureList() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        try {
            if (result != null) {
                mCallbacktask.CallBack(result);
            } else {
                mCallbacktask.CallBack(null);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        mCallbacktask = null;
    }

    @Override
    protected String doInBackground(Builder... params) {
        HttpGet request = new HttpGet(params[0].build().toString());
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        timeoutConnection = 10000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutConnection);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        try {
            HttpResponse response = httpClient.execute(request);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    break;
        	case HttpStatus.SC_REQUEST_TIMEOUT:
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){ // Read line by line
                sb.append(line);
            }
            accessResult = sb.toString();
        }
        catch (ConnectTimeoutException e){
            e.printStackTrace();
            return null;
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            request.abort();
            httpClient.getConnectionManager().shutdown();
        }
        return accessResult;
    }

    public void setOnCallBack(CallBackTask _cbj) {
        mCallbacktask = _cbj;
    }

    public static class CallBackTask {
        public void CallBack(String result) {
        }

        public void CallProgress(Integer progress) {
        }
    }
}
