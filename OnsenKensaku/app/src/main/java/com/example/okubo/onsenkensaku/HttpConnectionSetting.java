package com.example.okubo.onsenkensaku;

import android.content.Context;
import android.net.Uri;

/**
 * Created by Okubo on 9/29/2015 029.
 */
public class HttpConnectionSetting {

    private Context mContext;
    // 1km in Japan
    private final double LATITUDE_PER_KM = 0.0090133729745762;
    private final double LONGITUDE_PER_KM = 0.010966404715491394;

    public HttpConnectionSetting(Context context) {
        mContext = context;
    }

    public void startHttpRequestPrefectureList(HttpRequestPrefectureList httpRequestPrefectureList){

        String mURL = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URIPrefecture);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mURL);
        builder.path(mPath);
        httpRequestPrefectureList.execute(builder);
    }

    public void startHttpRequestOnsenInforByPrefecture(HttpRequestExecution asyncHttpRequest,String id){

        String mURL = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URIOnsenPre);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mURL);
        builder.path(mPath);
        builder.appendQueryParameter("prefecture", id);
//        builder.appendQueryParameter("limit", "2");

        asyncHttpRequest.execute(builder);
    }

    public void startHttpRequestOnsenInforByLocation(HttpRequestExecution asyncHttpRequest,double latitude, double longitude){

        String mURL = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URIOnsenLoc);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mURL);
        builder.path(mPath);
        builder.appendQueryParameter("point[]", String.valueOf(longitude + (LONGITUDE_PER_KM * 10)) + "," + String.valueOf(latitude + (LATITUDE_PER_KM * 10)));
        builder.appendQueryParameter("point[]", String.valueOf(longitude - (LONGITUDE_PER_KM * 10)) + "," + String.valueOf(latitude - (LATITUDE_PER_KM * 10)));
//        builder.appendQueryParameter("limit","100");

        asyncHttpRequest.execute(builder);
    }
}
