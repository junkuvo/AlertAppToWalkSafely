package apps.junkuvo.alertapptowalksafely.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

public class ColorUtil {

    public static void setColorIdOnDrawable(Context context, Drawable drawable, @ColorRes int colorResId) {
        int colorValue = ContextCompat.getColor(context, colorResId);
        drawable.setColorFilter(colorValue, PorterDuff.Mode.SRC_IN);
    }

    public static void setColorOnDrawable(Context context, Drawable drawable, @ColorInt int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

}
