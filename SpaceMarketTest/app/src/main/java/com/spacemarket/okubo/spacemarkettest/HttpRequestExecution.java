package com.spacemarket.okubo.spacemarkettest;

import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpRequestExecution extends AsyncTask<Builder, Void, JSONObject> {
    public interface AsyncCallback {
        void preExecute();
        void postExecute(JSONObject result);
        void cancel();
    }

    private AsyncCallback mAsyncCallback = null;

    public HttpRequestExecution(AsyncCallback _asyncCallback) {
        mAsyncCallback = _asyncCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mAsyncCallback.preExecute();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        mAsyncCallback.postExecute(result);
    }

    @Override
    protected JSONObject doInBackground(Builder... params) {
        HttpGet request = new HttpGet(params[0].build().toString());
        Log.d("test", params[0].build().toString());
//        HttpParams httpParameters = new BasicHttpParams();
//        int timeoutConnection = 5000;
//        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//        timeoutConnection = 10000;
//        HttpConnectionParams.setSoTimeout(httpParameters, timeoutConnection);
//        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse response = httpClient.execute(request);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    outputStream.close();
                    if(isCancelled()){
                        return null;
                    }
                    Log.d("test", outputStream.toString());
                    return new JSONObject(outputStream.toString() );

//                return new JSONObject("{events:" + outputStream.toString() + "}");
                case HttpStatus.SC_REQUEST_TIMEOUT:
                default:
                    response.getEntity().getContent().close();
            }
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
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mAsyncCallback.cancel();
    }
}
