package com.receipt_main;

import android.location.LocationListener;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;

import com.receipt.ReceiptsList;

import net.arnx.jsonic.JSON;

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

public class AsyncHttpRequest extends AsyncTask<Uri.Builder, Void, String> {
    
    private String accessResult = "";

    private static CallBackTask mCallbacktask;

    
    public AsyncHttpRequest() {
    }
    
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
    protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		// 3 receipt, 1 detail in each receipt
		result = "{'message':null,'receipts':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//		Log.d("test",result);
		//		result = "{'message':null,'receipts':[{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";

//		if (ItemListActivity.sReceiptList != null & ItemListActivity.sReceiptList.receipts != null){
//			if(ItemListActivity.sReceiptList.receipts.length == 3){
//				result = "{'message':null,'receipts':[{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//			}else{
//				result = "{'message':null,'receipts':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//			}
//		}else{
//			result = "{'message':null,'receipts':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','receiptLineNumber':'00001','sku':'662627','status':'0'}],'expectedReceiptDate':'11/15/2013 8:00:00 PM','externReceiptKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','receiptDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";			
//		}
		
		ItemListActivity.sReceiptList = JSON.decode(result,ReceiptsList.class);

		if(ItemListActivity.sReceiptList.getStatus().equals("SUCCESS") && mCallbacktask != null){
			mCallbacktask.CallBack(result);
			mCallbacktask = null;
		}
    }

	@Override
	protected String doInBackground(Builder... params) {
		HttpGet request = new HttpGet(params[0].build().toString());
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 1000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        timeoutConnection = 1000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutConnection);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

        try {
        	HttpResponse response = httpClient.execute(request);
        	switch (response.getStatusLine().getStatusCode()) {
        	case HttpStatus.SC_OK:
//        		accessResult =  String.valueOf(HttpStatus.SC_OK) +"\n"+ params[0].build().toString();
//        		Log.d("test","success"+accessResult);
        		break;
//        	case HttpStatus.SC_REQUEST_TIMEOUT:
//        		Log.d("test","timeout");
////        		accessResult = "Time out";
//        		break;
        	default:
//        		Log.d("test","fail" + String.valueOf(response.getStatusLine().getStatusCode()));
//        		accessResult = String.valueOf(response.getStatusLine().getStatusCode())  +"\n"+ params[0].build().toString();
        	}  
        	
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
       		StringBuilder sb = new StringBuilder();
       		String line = null;
       		while ((line = reader.readLine()) != null){ // Read line by line
       		    sb.append(line);
       		}
       		
       		accessResult = sb.toString();
       		
        }
        catch(ConnectTimeoutException e){
            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
        	e.printStackTrace();
        } finally {
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
