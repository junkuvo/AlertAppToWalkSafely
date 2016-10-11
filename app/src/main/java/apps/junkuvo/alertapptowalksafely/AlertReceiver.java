package apps.junkuvo.alertapptowalksafely;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static apps.junkuvo.alertapptowalksafely.MainActivity.CLICK_NOTIFICATION;
import static apps.junkuvo.alertapptowalksafely.MainActivity.DELETE_NOTIFICATION;

public class AlertReceiver extends BroadcastReceiver {
    private static final String TAG = AlertReceiver.class.getSimpleName();
    private final AlertReceiver self = this;

    private int mStepCount = 0;
    private static ReceiveEventListener receiveEventListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CLICK_NOTIFICATION)) {
            receiveEventListener.OnReceivedClick();
//            Intent startActivityIntent = new Intent(context, MainActivity.class);
//            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            context.startActivity(startActivityIntent);
            return;
        }

        if (intent.getAction().equals(DELETE_NOTIFICATION)) {
            receiveEventListener.OnReceivedDelete();
//            killAlertService();
//            finish();
            return;
        }

        receiveEventListener.OnReceivedStep(intent.getBooleanExtra("isStepCounter", false), intent.getIntExtra("stepCount", mStepCount));
//        if (intent.getBooleanExtra("isStepCounter", false)) {
//            if (shouldShowPedometer()) {
//                mStepCount = intent.getIntExtra("stepCount", mStepCount);
//                ((TextView) findViewById(R.id.txtStepCount)).setText(String.valueOf(mStepCount)
//                        + context.getString(R.string.home_step_count_dimension));
//            }
//        } else {
//            // 歩きスマホの注意
//            if (!shouldShowAlert() && mToastOn) {
//                String alertMessage = ((EditText) context.findViewById(R.id.txtAlertMessage)).getText().toString();
//                createToastShort(alertMessage).show();
//                setShouldShowAlert(true);
//            }
//
//            if (mVibrationOn) {
//                Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
//                vibrator.vibrate(300);
//            }
//        }
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
