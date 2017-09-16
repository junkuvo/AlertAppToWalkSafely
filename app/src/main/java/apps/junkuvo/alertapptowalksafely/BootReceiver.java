package apps.junkuvo.alertapptowalksafely;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean shouldShow = true;//SharedPreferencesUtil.getBoolean(context,context.getString(R.string.app_name), SharedPreferencesUtil.PrefKeys.NOTIFICATION_SHOW_IN_BAR.getKey(), false);

        if(shouldShow) {
            Intent i = new Intent(context, AlertService.class);
            context.startService(i);
        }
    }
}
