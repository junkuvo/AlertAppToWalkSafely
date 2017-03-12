package apps.junkuvo.alertapptowalksafely.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LINEUtil {

    public static void sendStringMessage(Context context, String message) {
        //Lineがインストールされているかチェック
        String appId = "jp.naver.line.android";

        String enc = "";
        try {
            //文字をエンコード
            enc = URLEncoder.encode(message, "utf-8");  //(エンコードしたい変数,"文字コード")
        } catch (UnsupportedEncodingException e) {
            //エンコード失敗時
            Toast.makeText(context, "LINEの起動がうまくいきませんでした。。", Toast.LENGTH_LONG).show();
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(appId, PackageManager.GET_META_DATA);
            //インストールされてたら、Lineへ
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("line://msg/text/" + enc));
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            //インストールされてなかったら、インストールを要求する
            Toast.makeText(context, "LINEがインストールされていないようです", Toast.LENGTH_LONG).show();
        }
    }
}
