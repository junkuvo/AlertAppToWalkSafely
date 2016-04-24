package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Utility {

    private Context mContext;

    public Utility(Context context) {
        mContext = context;
    }

    public int getOrientation() {
        Resources resources = mContext.getResources();
        Configuration config = resources.getConfiguration();
//        String str;
//        switch (config.orientation) {
//            case Configuration.ORIENTATION_PORTRAIT:
//                str = "縦方向";
//                break;
//            case Configuration.ORIENTATION_LANDSCAPE:
//                str = "横方向";
//                break;
//            default:
//                str = "デフォルト";
//        }
        return config.orientation;
    }

    public boolean isTabletNotPhone() {
        Resources r = mContext.getResources();
        Configuration configuration = r.getConfiguration();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
                return false;
            } else {
                return true;
            }
        } else {
            if (configuration.smallestScreenWidthDp < 600) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static String stringEncrypt(SecretKeySpec key, String target) throws Exception{
        // AESアルゴリズムでCipherオブジェクトを作成
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(target.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String stringDecrypt(SecretKeySpec key, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }

    /**
     * バージョンコードを取得する
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context){
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return versionCode;
    }

}
