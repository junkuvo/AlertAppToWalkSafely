package junkuvo.apps.androidutility;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.widget.Toast;

import junkuvo.apps.androidutility.view.EllipseBackgroundTextView;

public class ToastUtil {

    public static void showCustomToast(Context context, String text, int colorId, float textSpSize, int position) {

        Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        EllipseBackgroundTextView tv = new EllipseBackgroundTextView(context);
        tv.setmBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.shape_rounded));
        tv.setTextString(text);
        tv.setTextColor(ContextCompat.getColor(context, colorId));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSpSize);
        int toastPadding = context.getResources().getDimensionPixelSize(R.dimen.toast_padding);
        tv.setPadding(toastPadding, toastPadding, toastPadding, toastPadding);

        toast.setView(tv);
        int toastMargin = context.getResources().getDimensionPixelSize(R.dimen.toast_margin_top_bottom);
        toast.setGravity(position, 0, toastMargin);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
