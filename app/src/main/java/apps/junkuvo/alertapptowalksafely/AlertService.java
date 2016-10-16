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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import junkuvo.apps.androidutility.ToastUtil;

public class AlertService extends IntentService implements SensorEventListener, AlertReceiver.ReceiveEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION = "AlertService";
    private static final float TOAST_TEXT_SIZE = 32; // sp

    private SensorManager mSensorManager;
    private AlertReceiver mAlertReceiver = new AlertReceiver();

    // 歩きスマホの判定に利用（姿勢の計算）
    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    // 歩数カウントに利用（ステップセンサがない場合）
    private WalkCountCalculator mWalkCountCalculator;

    private int mAlertStartAngle;
    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    public Context mContext;
    // Recognition API用
    private GoogleApiClient mApiClient;
    private PendingIntent mReceiveRecognitionIntent;
    private final int ACTIVITY_RECOGNITION_CONFIDENCE = 10;
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

    // 歩数計
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;

    // 歩数表示のTextView
    private TextView mTxtStepCount;

    // 画面のON/OFFを検知する
    private boolean mIsScreenOn = true;
    private BroadcastReceiver screenStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receive screen off
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mIsScreenOn = false;
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mIsScreenOn = true;
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

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(getApplicationContext());
        mWalkCountCalculator = new WalkCountCalculator();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStatusReceiver, filter);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        mContext = getApplicationContext();
        mAlertReceiver.setOnReceiveEventListener(this);
        mHasStepFeature = isHasStepFeature();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStart(intent, startId);
        handleOnStart(intent, startId);
        ((AlertApplication) getApplication()).setIsRunningService(true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (screenStatusReceiver != null) {
            unregisterReceiver(screenStatusReceiver);
        }

        if (mApiClient != null && mApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mApiClient, mReceiveRecognitionIntent);
            mApiClient.disconnect();
        }
        Toast.makeText(getApplicationContext(), "destroy", Toast.LENGTH_LONG).show();
        ((AlertApplication) getApplication()).setIsRunningService(false);
    }

    private PendingIntent getPendingIntentWithBroadcast(String action) {
        return PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(action), 0);
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "bind", Toast.LENGTH_LONG).show();
        return mAlertServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(getApplicationContext(), "rebind", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(), "unbind", Toast.LENGTH_LONG).show();
        return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
    }

    // GoogleApiClient
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, AlertService.class);
        mReceiveRecognitionIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    private void handleOnStart(Intent intent, int startId) {
        startServiceForeground();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // センサーのオブジェクトリストを取得する
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // 歩数計用のセンサー登録
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // センサーの値が変化すると呼ばれる(加速度・ステップディテクター・ステップカウンター)
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 画面がONの場合、歩きスマホを検知する
        Sensor sensor = event.sensor;
        if (mIsScreenOn) {
            if (mStepCountBefore == 0) {
                mStepCountBefore = mStepCountCurrent;
            }
            // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
            // 5回カウントして1秒ごとに下記を実行する(1秒くらいあれば歩数が変化している前提)
            if (mTendencyCheckCount == 5) {
                // 歩行中であることを判定
                if (isWalking() || isWalkingStatus) {
                    // 歩いている状態で下記にてデバイス角度計算
                    int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
                    // 下向きかどうかの判定
                    // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
                    // さらにテーブルに置いたときなど、水平状態があり得るため tendency > 3(適当) とする
                    if ((tendency > 180 - getAlertStartAngle() || tendency < getAlertStartAngle()) && tendency > 3) {
                        mTendencyOutCount++;
                        //  下向きと判定されるのが連続5回の場合、Alertを表示させる
                        if (mTendencyOutCount == 5) {
                            // 歩数計センサの利用：
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("isStepCounter", false);
                            sendBroadcast(intent);
                            mTendencyOutCount = 0;

                            isWalkingStatus = false;
                        }
                    } else {
                        if (mTendencyOutCount > 0) {
                            mTendencyOutCount--;
//                            setShouldShowAlert(false);
                        }
                    }
                }
                mTendencyCheckCount = 0;
            }
        }

        // 歩数計の値を取得
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // 歩行センサがある場合
            mStepCountCurrent++;//Integer.valueOf(String.valueOf(values[0]));
            if (shouldShowPedometer()) {
                Intent intent = new Intent(ACTION);
                intent.putExtra("isStepCounter", true);
                intent.putExtra("stepCount", mStepCountCurrent);
                sendBroadcast(intent);
            }
        } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mIsScreenOn) {
                mTendencyCheckCount++;
            }
            // 歩行センサがない場合 3軸加速度から計算
            if (!mHasStepFeature) {
                mStepCountCurrent = mWalkCountCalculator.walkCountCalculate(event);
                Intent intent = new Intent(ACTION);
                intent.putExtra("isStepCounter", true);
                intent.putExtra("stepCount", mStepCountCurrent);
                sendBroadcast(intent);
            }
        }
    }

    private int mStepCountBefore = 0;
    private int mStepCountAfter = 0;
    private int mStepCountCurrent = 0;

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

    private void detectWalkingStatusByGcm(List<DetectedActivity> probableActivities) {
        int confidence = 0;
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
                case DetectedActivity.UNKNOWN:
                    isWalkingStatus = false;
                    Log.e("ActivityRecognition", "others: " + activity.getConfidence());
                    break;
            }
            isWalkingStatus = (confidence >= ACTIVITY_RECOGNITION_CONFIDENCE);
        }
    }

    public void startServiceForeground() {
        // サービスを永続化するために、通知を作成する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("歩きスマホ防止アプリ起動！");
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("アプリを開く際はタップしてください");
//        builder.setSubText("アプリを開く際はタップしてください");
        builder.setSmallIcon(R.drawable.ic_stat_small);
        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        // FIXME : service から unbindする方法がないので、Notification から停止させる機能は一旦なくす
//        builder.addAction(R.drawable.ic_stat_small, getString(R.string.home_button_stop), getPendingIntentWithBroadcast(AlertReceiver.DELETE_NOTIFICATION));

        builder.setContentIntent( //通知タップ時のPendingIntent
                getPendingIntentWithBroadcast(AlertReceiver.CLICK_NOTIFICATION)
        );
        builder.setDeleteIntent(  //通知の削除時のPendingIntent
                getPendingIntentWithBroadcast(AlertReceiver.DELETE_NOTIFICATION)
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

    @Override
    public void OnReceivedClick() {
        Intent startActivityIntent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(startActivityIntent);
    }

    @Override
    public void OnReceivedDelete() {
        // FIXME : unbindはどうする？
        // service から unbindする方法がないので、Notification から停止させる機能は一旦なくす
        stopSelf();
    }

    @Override
    public void OnReceivedStep(boolean isStepCounter, int stepCount) {
        if (isStepCounter) {
            mStepCountCurrent = stepCount;
            if (shouldShowPedometer()) {
                // TODO : ServiceからUIいじりたい
                if(mTxtStepCount != null) {
                    mTxtStepCount.setText(String.valueOf(mStepCountCurrent) + getString(R.string.home_step_count_dimension));
                }
            }
        } else {
            // 歩きスマホの注意
            if (IsToastOn()) {
//                if (shouldShowAlert() && IsToastOn()) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showCustomToast(getApplicationContext(), mAlertMessage, R.color.colorAccent, TOAST_TEXT_SIZE, mToastPosition);
                    }
                });
            }

            if (IsVibrationOn()) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(300);
            }
        }
    }

    public boolean shouldShowAlert() {
        return mShouldShowAlert;
    }

    public void setShouldShowAlert(boolean shouldShowAlert) {
        mShouldShowAlert = shouldShowAlert;
    }

    public boolean shouldShowPedometer() {
        return mShouldShowPedometer;
    }

    public void setShouldShowPedometer(boolean shouldShowPedometer) {
        mShouldShowPedometer = shouldShowPedometer;
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

    public TextView getTxtStepCount() {
        return mTxtStepCount;
    }

    public void setTxtStepCount(TextView txtStepCount) {
        this.mTxtStepCount = txtStepCount;
    }

    public int getStepCountCurrent() {
        return mStepCountCurrent;
    }

    public void setStepCountCurrent(int mStepCountCurrent) {
        this.mStepCountCurrent = mStepCountCurrent;
    }
}

