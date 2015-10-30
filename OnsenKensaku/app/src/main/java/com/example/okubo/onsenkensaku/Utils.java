package com.example.okubo.onsenkensaku;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Okubo on 9/29/2015 029.
 */
public class Utils {

    public String convertToString(String unicode)
    {
        String[] codeStrs = unicode.split("\\\\u");
        StringBuffer encodedText = new StringBuffer();
        int[] codePoints = new int[1];
        for (int i = 0; i < codeStrs.length - 1; i++) {
            if(codeStrs[i + 1].length() == 4) {
                codePoints[0] = Integer.parseInt(codeStrs[i + 1], 16);
                encodedText.append(new String(codePoints, 0, codePoints.length));
            }else if (codeStrs[i + 1].length() > 4){
                codePoints[0] = Integer.parseInt(codeStrs[i + 1].substring(0,4), 16);
                encodedText.append(new String(codePoints, 0, codePoints.length));
                encodedText.append(codeStrs[i + 1].substring(4,codeStrs[i + 1].length()));
            }else{
                encodedText.append(codeStrs[i + 1]);
            }
        }
        return codeStrs[0] + encodedText.toString();
    }

    public boolean checkNetworkStatus(Context context){
        ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        if (nInfo == null) {
            return false;
        }

        if (nInfo.isConnected()) {
            if (nInfo.getTypeName().equals("WIFI")) {
            } else if (nInfo.getTypeName().equals("mobile")) {
            }

        } else {
            return false;
        }

        return true;
    }
}
