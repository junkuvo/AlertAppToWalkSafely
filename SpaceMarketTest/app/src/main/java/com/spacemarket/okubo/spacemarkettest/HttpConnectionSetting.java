package com.spacemarket.okubo.spacemarkettest;

import android.content.Context;
import android.net.Uri;

/**
 * Created by Okubo on 9/29/2015 029.
 */
public class HttpConnectionSetting {

    private Context mContext;

    public HttpConnectionSetting(Context context) {
        mContext = context;
    }

    public void startHttpRequestSpaceData(HttpRequestExecution asyncHttpRequest){

        String mURL = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URISpace);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mURL);
        builder.path(mPath);

        asyncHttpRequest.execute(builder);
    }

    public void startHttpRequestRoomData(HttpRequestExecution asyncHttpRequest){

        String mURL = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URIRoom);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mURL);
        builder.path(mPath);

        asyncHttpRequest.execute(builder);
    }
}
