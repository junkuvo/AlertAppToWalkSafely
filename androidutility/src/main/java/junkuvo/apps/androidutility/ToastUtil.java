package junkuvo.apps.androidutility;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {
    private static final String TAG = ToastUtil.class.getSimpleName();
    private final ToastUtil self = this;

    public static void showCustomToast(Context context, String text, int colorId, float textSpSize, int position) {
        Toast toast = new Toast(context);
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(context, colorId));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSpSize);
        int toastMargin = context.getResources().getDimensionPixelSize(R.dimen.toast_margin_top_bottom);
        tv.setPadding(0, toastMargin, 0, toastMargin);
        toast.setView(tv);
        toast.setGravity(position, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
