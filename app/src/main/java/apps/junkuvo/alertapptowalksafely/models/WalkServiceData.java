package apps.junkuvo.alertapptowalksafely.models;

import com.github.stkent.amplify.utils.StringUtils;

public class WalkServiceData {

    public static final String CLICK_NOTIFICATION = "walk_safe_click_notification";
    public static final String DELETE_NOTIFICATION = "walk_safe_delete_notification";

    private static WalkServiceData walkServiceData = new WalkServiceData();
    private String walkCountAll = "";
    private String walkCountAlert = "";
    private boolean isRunningService = false;

    private boolean mIsToastOn = true;
    private boolean mIsVibrationOn = true;
    private int mToastPosition;
    private int mAlertStartAngle;
    private String mAlertMessage;


    private WalkServiceData() {
    }

    public static WalkServiceData getInstance(){
        return walkServiceData;
    }

    public String getWalkCountAll() {
        return walkCountAll;
    }

    public int getWalkCountAllInt() {
        if(StringUtils.isBlank(walkCountAll)){
            return 0;
        }else{
            return Integer.valueOf(walkCountAll);
        }
    }

    public void setWalkCountAll(String walkCountAll) {
        this.walkCountAll = walkCountAll;
    }

    public String getWalkCountAlert() {
        return walkCountAlert;
    }

    public int getWalkCountAlertInt() {
        if(StringUtils.isBlank(walkCountAlert)){
            return 0;
        }else{
            return Integer.valueOf(walkCountAlert);
        }
    }


    public void setWalkCountAlert(String walkCountAlert) {
        this.walkCountAlert = walkCountAlert;
    }

    public boolean isRunningService() {
        return isRunningService;
    }

    public void setRunningService(boolean runningService) {
        isRunningService = runningService;
    }

    public boolean IsToastOn() {
        return mIsToastOn;
    }

    public void setIsToastOn(boolean mIsToastOn) {
        this.mIsToastOn = mIsToastOn;
    }

    public boolean IsVibrationOn() {
        return mIsVibrationOn;
    }

    public void setIsVibrationOn(boolean mIsVibrationOn) {
        this.mIsVibrationOn = mIsVibrationOn;
    }

    public int getToastPosition() {
        return mToastPosition;
    }

    public void setToastPosition(int mToastPosition) {
        this.mToastPosition = mToastPosition;
    }

    public int getAlertStartAngle() {
        return mAlertStartAngle;
    }

    public void setAlertStartAngle(int mAlertStartAngle) {
        this.mAlertStartAngle = mAlertStartAngle;
    }

    public String getAlertMessage() {
        return mAlertMessage;
    }

    public void setAlertMessage(String mAlertMessage) {
        this.mAlertMessage = mAlertMessage;
    }

}
