package apps.junkuvo.alertapptowalksafely;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertReceiver extends BroadcastReceiver {
    public static final String CLICK_NOTIFICATION = "walk_safe_click_notification";
    public static final String DELETE_NOTIFICATION = "walk_safe_delete_notification";

    private int mStepCount = 0;
    static private ReceiveEventListener receiveEventListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CLICK_NOTIFICATION)) {
            receiveEventListener.OnReceivedClick();
            return;
        }

        if (intent.getAction().equals(DELETE_NOTIFICATION)) {
            receiveEventListener.OnReceivedDelete();
            return;
        }

        receiveEventListener.OnReceivedStep(intent.getBooleanExtra("isStepCounter", false), intent.getIntExtra("stepCount", mStepCount));
    }

    public interface ReceiveEventListener {
        void OnReceivedClick();

        void OnReceivedDelete();

        void OnReceivedStep(boolean isStepCounter, int stepCount);
    }

    public void setOnReceiveEventListener(ReceiveEventListener l) {
        receiveEventListener = l;
    }
}
