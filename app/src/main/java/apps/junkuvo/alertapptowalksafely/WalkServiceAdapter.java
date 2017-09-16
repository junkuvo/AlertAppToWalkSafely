package apps.junkuvo.alertapptowalksafely;

import apps.junkuvo.alertapptowalksafely.models.WalkServiceData;

import static apps.junkuvo.alertapptowalksafely.models.WalkServiceData.DELETE_NOTIFICATION;
import static apps.junkuvo.alertapptowalksafely.models.WalkServiceData.getInstance;

public class WalkServiceAdapter {

    public void notifyWalkDataAlertChanged(int walkAlertCount){
        WalkServiceData.getInstance().setWalkCountAlert(String.valueOf(walkAlertCount));
        onWalkDataChangedListener.onWalkDataAlertChanged(String.valueOf(walkAlertCount));
    }
    public void notifyWalkDataNormalChanged(int walkNormalCount){
        WalkServiceData.getInstance().setWalkCountAll(String.valueOf(walkNormalCount));
        onWalkDataChangedListener.onWalkDataNormalChanged(String.valueOf(walkNormalCount));
    }

    public void notifyActionFromNotification(String action){
        if(action.equals(DELETE_NOTIFICATION)) {
            WalkServiceData.getInstance().setRunningService(false);
            OnActionFromNotificationListener.onStopFromNotification(action);
        }
    }

    /**
     * サービスが動いているかどうかを返す
     * @return
     */
    public boolean isWalkServiceRunning(){
        return getInstance().isRunningService();
    }

    private OnWalkDataChangedListener onWalkDataChangedListener;
    interface OnWalkDataChangedListener{
        void onWalkDataAlertChanged(String walkCountAlert);

        void onWalkDataNormalChanged(String walkCountNormal);
    }

    public void setOnWalkDataChangedListener(OnWalkDataChangedListener onWalkDataChangedListener) {
        this.onWalkDataChangedListener = onWalkDataChangedListener;
    }

    private OnActionFromNotificationListener OnActionFromNotificationListener;
    public interface OnActionFromNotificationListener {
        void onStopFromNotification(String action);
    }

    public void setOnActionFromNotificationListener(OnActionFromNotificationListener onActionromNotificationListener) {
        this.OnActionFromNotificationListener = onActionromNotificationListener;
    }

    public void removeOnActionFromNotificationListener() {
        this.OnActionFromNotificationListener = null;
    }

    private OverlayActionListener overlayActionListener;
    interface OverlayActionListener {
        void onOpenApp();
    }

    public void setOverlayActionListener(OverlayActionListener overlayActionListener) {
        this.overlayActionListener = overlayActionListener;
    }

    public OverlayActionListener getOverlayActionListener() {
        return overlayActionListener;
    }
}
