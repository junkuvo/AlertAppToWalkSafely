package com.shipment_main;

import android.location.LocationListener;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.util.Log;

import com.shipment.ShipmentsList;

import net.arnx.jsonic.JSON;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
		
		// 3 shipment, 1 detail in each shipment
//		result = "{'message':null,'shipments':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//		Log.d("test",result);
				result = "{'message':null,'shipments':[{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";

//		if (ItemListActivity.sShipmentList != null & ItemListActivity.sShipmentList.shipments != null){
//			if(ItemListActivity.sShipmentList.shipments.length == 3){
//				result = "{'message':null,'shipments':[{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//			}else{
//				result = "{'message':null,'shipments':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//			}
//		}else{
//			result = "{'message':null,'shipments':[{'details':[{'description':'662625','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'aaaaa662626','status':'9'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000000000','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 7:15:14 PM','status':'0','supplierName':'aaaaaaaaaaaa'},{'details':[{'description':'662626','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'3110134662528','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''},{'details':[{'description':'662627','qtyExpected':'5.00000','qtyReceived':'0.00000','shipmentLineNumber':'00001','sku':'662627','status':'0'}],'expectedShipmentDate':'11/15/2013 8:00:00 PM','externShipmentKey':'9111000662627','qtyExpected':'1000','qtyReceived':'1000','shipmentDate':'2/14/2014 8:21:39 PM','status':'0','supplierName':''}],'status':'SUCCESS'}";
//		}
		
		ItemListActivity.sShipmentList = JSON.decode(result,ShipmentsList.class);

		if(ItemListActivity.sShipmentList.getStatus().equals("SUCCESS") && mCallbacktask != null){
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
        Log.d("test",params[0].build().toString());
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
