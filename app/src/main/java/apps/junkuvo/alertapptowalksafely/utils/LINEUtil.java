package apps.junkuvo.alertapptowalksafely.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class LINEUtil {

    public static void sendStringMessage(Context context, String message) {
        //Lineがインストールされているかチェック
        String appId = "jp.naver.line.android";
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(appId, PackageManager.GET_META_DATA);
            //インストールされてたら、Lineへ
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("line://msg/text/" + message));
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            //インストールされてなかったら、インストールを要求する
        }
    }
}
