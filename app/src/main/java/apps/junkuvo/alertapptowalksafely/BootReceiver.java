package apps.junkuvo.alertapptowalksafely;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;

import apps.junkuvo.alertapptowalksafely.models.WalkServiceData;
import junkuvo.apps.androidutility.SharedPreferencesUtil;

import static apps.junkuvo.alertapptowalksafely.MainActivity.EXTRA_KEY_CAN_SHOW_OVERLAY_FLAG;
import static apps.junkuvo.alertapptowalksafely.MainActivity.SETTING_SHAREDPREF_NAME;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // FIXME 局所化
        // サービスの起動に必要な設定の読み込み
        boolean shouldShow = SharedPreferencesUtil.getBoolean(context,SETTING_SHAREDPREF_NAME, SharedPreferencesUtil.PrefKeys.BOOT_RUN.getKey(), false);
        boolean isToastOn = SharedPreferencesUtil.getBoolean(context, SETTING_SHAREDPREF_NAME, "message", true);
        boolean isVibrationOn = SharedPreferencesUtil.getBoolean(context, SETTING_SHAREDPREF_NAME, "vibrate", true);
        int toastPosition = SharedPreferencesUtil.getInt(context, SETTING_SHAREDPREF_NAME, "toastPosition", Gravity.CENTER);
        int ALERT_ANGLE_INITIAL_OFFSET = 15;
        int ALERT_ANGLE_INITIAL_VALUE = 30;
        int alertStartAngle = SharedPreferencesUtil.getInt(context, SETTING_SHAREDPREF_NAME, "progress", ALERT_ANGLE_INITIAL_VALUE) + ALERT_ANGLE_INITIAL_OFFSET;
        String alertMessage = SharedPreferencesUtil.getString(context, SETTING_SHAREDPREF_NAME, "alert_message");
        WalkServiceData.getInstance().setIsToastOn(isToastOn);
        WalkServiceData.getInstance().setIsVibrationOn(isVibrationOn);
        WalkServiceData.getInstance().setToastPosition(toastPosition);
        WalkServiceData.getInstance().setAlertStartAngle(alertStartAngle + ALERT_ANGLE_INITIAL_OFFSET);
        WalkServiceData.getInstance().setAlertMessage(alertMessage);

        if(shouldShow) {
            Intent i = new Intent(context, AlertService.class);
            i.putExtra(EXTRA_KEY_CAN_SHOW_OVERLAY_FLAG, checkOverlayPermission(context));
            context.startService(i);
        }
    }

    // FIXME 局所化
    private boolean checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /** check if we already  have permission to draw over other apps */
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
}
