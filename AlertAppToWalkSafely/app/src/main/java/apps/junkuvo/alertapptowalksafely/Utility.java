package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

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
}
