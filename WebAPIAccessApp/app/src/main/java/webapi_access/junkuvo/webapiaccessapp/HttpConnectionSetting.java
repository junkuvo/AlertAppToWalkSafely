package webapi_access.junkuvo.webapiaccessapp;

import android.content.Context;
import android.net.Uri;

public class HttpConnectionSetting {

    private Context mContext;

    public HttpConnectionSetting(Context context) {
        mContext = context;
    }

    public void startHttpRequestSpaceData(HttpRequestExecution asyncHttpRequest){

        String mDomain = mContext.getString(R.string.domain);
        String mPath = mContext.getString(R.string.URI);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getString(R.string.protocol));
        builder.encodedAuthority(mDomain);
        builder.path(mPath);

        asyncHttpRequest.execute(builder);
    }
}
