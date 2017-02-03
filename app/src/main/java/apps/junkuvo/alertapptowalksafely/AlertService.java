package apps.junkuvo.alertapptowalksafely;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import junkuvo.apps.androidutility.ToastUtil;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AlertService extends IntentService implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION = "AlertService";
    private static final float TOAST_TEXT_SIZE = 24; // sp

    private SensorManager mSensorManager;

    // 歩きスマホの判定に利用（姿勢の計算）
    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    // 歩数カウントに利用（ステップセンサがない場合）
    private WalkCountCalculator mWalkCountCalculator;
    private WalkCountCalculator mWalkCountCalculatorAsNg;

    private int mAlertStartAngle;
    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    public Context mContext;
    // Recognition API用
    private GoogleApiClient mApiClient;
    private PendingIntent mReceiveRecognitionIntent;
    private final int ACTIVITY_RECOGNITION_CONFIDENCE = 15;
    private boolean isWalkingStatus = false;

    private boolean mShouldShowAlert = false;
    private boolean mShouldShowPedometer = true;
    private boolean mIsToastOn = true;
    private boolean mIsVibrationOn = true;
    private int mToastPosition;
    private boolean mHasStepFeature = false;
    private String mAlertMessage;

    // サービスがバインドされているかどうかのフラグ
    private boolean mIsBoundService = false;

    // 歩きスマホ検知を開始しているかどうかのフラグ
    private boolean mIsRunningAlertService = false;

    // 歩数計
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;

    public static final String CLICK_NOTIFICATION = "walk_safe_click_notification";
    public static final String DELETE_NOTIFICATION = "walk_safe_delete_notification";

    // 画面のON/OFFを検知する
    private boolean mIsScreenOn = true;

    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int stepCount = intent.getIntExtra("stepCount", mStepCountCurrent);
            if (intent.getBooleanExtra("isStepCounter", false)) {
//                mStepCountCurrent = stepCount < 0 ? 0 : stepCount;
//                onWalkStepListener.onWalkStep(mStepCountCurrent);
            } else {
                // 歩きスマホの注意
                if (IsToastOn()) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showCustomToastWithImage(getApplicationContext(), mAlertMessage, R.color.fab_material_white, TOAST_TEXT_SIZE, mToastPosition);
                        }
                    });
                }

                if (IsVibrationOn()) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
                    vibrator.vibrate(pattern, -1);
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receive screen off
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mIsScreenOn = false;
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mIsScreenOn = true;
            }
            if (intent.getAction().equals(CLICK_NOTIFICATION)) {
                Intent startActivityIntent = new Intent(mContext, MainActivity.class);
                startActivityIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(startActivityIntent);
                return;
            }

            if (intent.getAction().equals(DELETE_NOTIFICATION)) {
                // service から unbindする方法がないので、Notification から停止させる機能は一旦なくす
                // stopSelf();
                stopSensors();
                onActionFromNotificationListener.onStopFromNotification(DELETE_NOTIFICATION);
                return;
            }
        }
    };

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AlertService(String name) {
        super(name);
    }

    public AlertService() {
        super("AlertService");
//        getSettingsInPreferences();
    }

    //サービスに接続するためのBinder
    public class AlertServiceBinder extends Binder {
        //サービスの取得
        AlertService getService() {
            return AlertService.this;
        }
    }

    //Binderの生成
    private final IBinder mAlertServiceBinder = new AlertServiceBinder();

    private IntentFilter intentFilter = new IntentFilter();
    private IntentFilter localIntentFilter = new IntentFilter();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        boolean isTablet = Utility.isTabletNotPhone(mContext);
        int orientation = Utility.getOrientation(mContext);
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(isTablet, orientation);
        mWalkCountCalculator = new WalkCountCalculator();
        mWalkCountCalculatorAsNg = new WalkCountCalculator();

        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(CLICK_NOTIFICATION);
        intentFilter.addAction(DELETE_NOTIFICATION);
        localIntentFilter.addAction(ACTION);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mHasStepFeature = isHasStepFeature();
    }

    /**
     * これはonStartServiceでしか呼ばれない
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        super.onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSensors();
    }

    private PendingIntent getPendingIntentWithBroadcast(String action) {
        return PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(action), FLAG_UPDATE_CURRENT);
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
        return mAlertServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
    }

    // GoogleApiClient
    // mApiClient.connect()のcallback
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, AlertService.class);
        mReceiveRecognitionIntent = PendingIntent.getService(this, 0, intent, FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 0, mReceiveRecognitionIntent);
    }

    // GoogleApiClient
    @Override
    public void onConnectionSuspended(int i) {

    }

    // GoogleApiClient
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            detectWalkingStatusByGcm(result.getProbableActivities());
        }
    }

    // センサーの精度が変更されると呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startSensors() {
        startServiceForeground();
        try {
            setIsRunningAlertService(true);

            LocalBroadcastManager.getInstance(mContext).registerReceiver(localBroadcastReceiver, localIntentFilter);
            registerReceiver(broadcastReceiver, intentFilter);

            // FIXME : RecognitionAPIの精度・更新頻度がよくわからない。connectしたタイミングでも謎の値が入ってくるので一旦利用をやめる。
//        mApiClient.connect();

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            // センサーのオブジェクトリストを取得する
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) {
                Sensor s = sensors.get(0);
                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }

            // 歩数計用のセンサー登録
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

            initializeSensingValues();
        } catch (Exception e) {
            stopSensors();
            e.printStackTrace();
        }
    }

    public void stopSensors() {
        try {
            // Notificationを消す
            stopForeground(true);

            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
                mSensorManager = null;
            }
            if (localBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(localBroadcastReceiver);
            }

            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }

            if (mApiClient != null && mApiClient.isConnected()) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mApiClient, mReceiveRecognitionIntent);
                mApiClient.disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            setIsRunningAlertService(false);
        }
    }

    public void initializeSensingValues() {
        mTendencyCheckCount = 0;
        mTendencyOutCount = 0;
        isWalkingStatus = false;
        mStepCountCurrent = -1;
        countAsNg = -1;
    }

    // 歩きスマホ中として歩数をカウントするかどうかのフラグ
    private boolean shouldCountAsNg = false;
    // 歩きスマホ中の歩数。画面に表示される
    private int countAsNg = -1;

    // センサーの値が変化すると呼ばれる(加速度・ステップディテクター・ステップカウンター)
    @Override
    public void onSensorChanged(final SensorEvent event) {
        // 画面がONの場合、歩きスマホを検知する
        final Sensor sensor = event.sensor;
        if (mIsRunningAlertService) {
            if (mIsScreenOn) {
                int stepCount = countAsNg < 0 ? 0 : countAsNg;
                onWalkStepListener.onWalkStep(stepCount);

                if (mStepCountBefore == 0) {
                    mStepCountBefore = mStepCountCurrent;
                }

                if (isEveryOneSecond()) {
                    // 歩行中であることを判定
                    // FIXME : RecognitionAPIの精度・更新頻度がよくわからない。connectしたタイミングでも謎の値が入ってくるので一旦利用をやめる。
                    if (isWalking()) {// || isWalkingStatus) {

                        // 歩いている状態で下記にてデバイス角度計算
                        int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
                        // 下向きかどうかの判定
                        // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
                        // さらにテーブルに置いたときなど、水平状態があり得るため tendency > (適当) とする
                        if ((tendency > 180 - getAlertStartAngle() || tendency < getAlertStartAngle()) && tendency > 8) {
                            mTendencyOutCount++;
                            shouldCountAsNg = true;
                            //  下向きと判定されるのが連続 n 回(= n 秒)の場合、Alertを表示させる
                            if (mTendencyOutCount == 5) {
                                // 歩数計センサの利用：
                                Intent intent = new Intent(ACTION);
                                intent.putExtra("isStepCounter", false);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);

                                mTendencyOutCount = 0;
                                isWalkingStatus = false;
                                mStepCountBefore = mStepCountCurrent;
                            }
                        } else {
                            if (mTendencyOutCount > 0) {
                                mTendencyOutCount--;
                            }
                            shouldCountAsNg = false;
                        }
                    }
                    mTendencyCheckCount = 0;
                }
            } else {
                // FIXME : 本来はScreenがOffの場合、センサを停止したい
                shouldCountAsNg = false;
                mTendencyCheckCount = 0;
                mTendencyOutCount = 0;
            }

            // 歩数計の値を取得
            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                mStepCountCurrent++;
                if (shouldCountAsNg) {
                    countAsNg++;
                }
            } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (mIsScreenOn) {
                    // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
                    // 5回カウントして1秒ごとに下記を実行する(1秒くらいあれば歩数が変化している前提)
                    mTendencyCheckCount++;
                }
                // 歩行センサがない場合 3軸加速度から計算
                if (!mHasStepFeature) {
                    mStepCountCurrent = mWalkCountCalculator.walkCountCalculate(event);
                    if (shouldCountAsNg) {
                        countAsNg = mWalkCountCalculatorAsNg.walkCountCalculate(event);
                    }
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("isStepCounter", true);
                    intent.putExtra("stepCount", mStepCountCurrent);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                }
            }
        }
    }

    private int mStepCountBefore = 0;
    private int mStepCountAfter = 0;
    // onSensorChanged の TYPE_STEP_DETECT が最初に1回よばれるのでその分-1
    private int mStepCountCurrent = -1;

    // 歩数の変化を計算して、変化があれば歩行中と判定
    private boolean isWalking() {
        mStepCountAfter = mStepCountCurrent;
        if (mStepCountBefore == mStepCountAfter) {
            return false;
        } else {
            mStepCountBefore = mStepCountAfter;
            return true;
        }
    }

    private boolean isEveryOneSecond() {
        return mTendencyCheckCount == 5;
    }

    private void detectWalkingStatusByGcm(List<DetectedActivity> probableActivities) {
        int confidence = 0;
        int confidenceOthers = 0;
        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.ON_FOOT: {
                    confidence = confidence + activity.getConfidence();
                    Log.e("ActivityRecognition", "On Foot: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    confidence = confidence + activity.getConfidence();
                    Log.e("ActivityRecognition", "Running: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    confidence = confidence + activity.getConfidence();
                    Log.e("ActivityRecognition", "Walking: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL:
                case DetectedActivity.IN_VEHICLE:
                case DetectedActivity.ON_BICYCLE:
                case DetectedActivity.TILTING:
                    confidenceOthers = confidenceOthers + activity.getConfidence();
                case DetectedActivity.UNKNOWN:
                    Log.e("ActivityRecognition", "others: " + activity.getConfidence());
                    isWalkingStatus = false;
                    break;
            }
            isWalkingStatus = (confidence >= ACTIVITY_RECOGNITION_CONFIDENCE
                    && confidenceOthers <= ACTIVITY_RECOGNITION_CONFIDENCE);
        }
    }

    public void startServiceForeground() {
        // サービスを永続化するために、通知を作成する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(getString(R.string.notification_ticker));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification_content_text));
//        builder.setSubText("アプリを開く際はタップしてください");
        builder.setSmallIcon(R.drawable.ic_stat_small);
        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        // FIXME : service から unbindする方法がないので、Notification から停止させる機能は一旦なくす
        builder.addAction(R.drawable.ic_stat_small, getString(R.string.home_button_stop), getPendingIntentWithBroadcast(DELETE_NOTIFICATION));

        builder.setContentIntent( //通知タップ時のPendingIntent
                getPendingIntentWithBroadcast(CLICK_NOTIFICATION)
        );
        builder.setDeleteIntent(  //通知の削除時のPendingIntent
                getPendingIntentWithBroadcast(DELETE_NOTIFICATION)
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.BigTextStyle notificationBigTextStyle = new NotificationCompat.BigTextStyle(builder);
            builder.setStyle(notificationBigTextStyle);
        }
        // ロックスクリーン上でどう見えるか
        builder.setVisibility(Notification.VISIBILITY_SECRET);

        // PRIORITY_MINだとどこにも表示されなくなる
        builder.setPriority(Notification.PRIORITY_MIN);
        // サービス永続化
        startForeground(R.string.app_name, builder.build());
    }

    public interface onWalkStepListener {
        void onWalkStep(int stepCount);
    }

    public void setOnWalkStepListener(onWalkStepListener onWalkStepListener) {
        this.onWalkStepListener = onWalkStepListener;
    }

    public void removeOnWalkStepListener() {
        this.onWalkStepListener = null;
    }

    public interface onActionFromNotificationListener {
        void onStopFromNotification(String action);
    }

    public void setOnActionFromNotificationListener(onActionFromNotificationListener onActionromNotificationListener) {
        this.onActionFromNotificationListener = onActionromNotificationListener;
    }

    public void removeOnActionFromNotificationListener() {
        this.onActionFromNotificationListener = null;
    }

    private onWalkStepListener onWalkStepListener;
    private onActionFromNotificationListener onActionFromNotificationListener;

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

    public boolean isHasStepFeature() {
        PackageManager packageManager = this.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    // TODO : ここTrueしか帰らないからMainActivityに移動した方がいい
    public boolean isBoundService() {
        return mIsBoundService;
    }

    public void setIsBoundService(boolean isBoundService) {
        this.mIsBoundService = isBoundService;
    }

    public int getStepCountCurrent() {
        return mStepCountCurrent;
    }

    public void setStepCountCurrent(int mStepCountCurrent) {
        this.mStepCountCurrent = mStepCountCurrent;
    }

    public boolean IsRunningAlertService() {
        return mIsRunningAlertService;
    }

    public void setIsRunningAlertService(boolean mIsRunningAlertService) {
        this.mIsRunningAlertService = mIsRunningAlertService;
    }
}

