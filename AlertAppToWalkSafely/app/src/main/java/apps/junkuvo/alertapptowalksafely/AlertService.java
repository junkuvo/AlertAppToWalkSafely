package apps.junkuvo.alertapptowalksafely;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class AlertService extends IntentService implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String ACTION = "Alert Service";
    private SensorManager mSensorManager;

    // 歩きスマホの判定に利用（姿勢の計算）
    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    // 歩数カウントに利用（ステップセンサがない場合）
    private WalkCountCalculator mWalkCountCalculator;

    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    // Recognition API用
    private GoogleApiClient mApiClient;
    private PendingIntent mReceiveRecognitionIntent;
    private final int ACTIVITY_RECOGNITION_CONFIDENCE = 10;
    private boolean isWalkingStatus = false;

    // 歩数計
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;

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
    }

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
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        handleOnStart(intent,startId);
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        onStart(intent, startId);
        handleOnStart(intent,startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if(screenStatusReceiver != null) {
            unregisterReceiver(screenStatusReceiver);
        }

        if (mApiClient != null && mApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mApiClient, mReceiveRecognitionIntent);
            mApiClient.disconnect();
        }
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
        return new AlertBinder();
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
    }

    // GoogleApiClient
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, AlertService.class );
        mReceiveRecognitionIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 0, mReceiveRecognitionIntent );
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
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    class AlertBinder extends Binder {
        AlertService getService() {
            return AlertService.this;
        }
    }

    // センサーの精度が変更されると呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void handleOnStart(Intent intent, int startId){
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

    // センサーの値が変化すると呼ばれる
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 画面がONの場合、歩きスマホを検知する
        Sensor sensor = event.sensor;
        if(mIsScreenOn) {
            if (mStepCountBefore == 0) {
                mStepCountBefore = mStepCountCurrent;
            }
            // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
            // 5回カウントして1秒ごとに下記を実行する(1秒くらいあれば歩数が変化している前提)
            if (mTendencyCheckCount >= 4) {
                // 歩行中であることを判定
                if (isWalking() || isWalkingStatus) {
                    // 歩いている状態で下記にてデバイス角度計算
                    int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
                    // 下向きかどうかの判定
                    // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
                    // さらにテーブルに置いたときなど、水平状態があり得るため tendency > 3(適当) とする
                    if ((tendency > 180 - MainActivity.sAlertStartAngle || tendency < MainActivity.sAlertStartAngle) && tendency > 3) {
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
                            MainActivity.sAlertShowFlag = false;
                        }
                    }
                    mTendencyCheckCount = 0;
                }
            }
        }

        // 歩数計の値を取得
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // 歩行センサがある場合
            mStepCountCurrent++;//Integer.valueOf(String.valueOf(values[0]));
            if(MainActivity.sPedometerFlag) {
                Intent intent = new Intent(ACTION);
                intent.putExtra("isStepCounter", true);
                intent.putExtra("stepCount",mStepCountCurrent);
                sendBroadcast(intent);
            }
        }else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mTendencyCheckCount++;
            // 歩行センサがない場合 3軸加速度から計算
            if(!MainActivity.mHasStepFeature) {
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
    private boolean isWalking(){
        mStepCountAfter = mStepCountCurrent;
        if(mStepCountBefore == mStepCountAfter){
            return false;
        }else{
            mStepCountBefore = mStepCountAfter;
            return true;
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        int confidence = 0;
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.ON_FOOT: {
//                    if( activity.getConfidence() >= ACTIVITY_RECOGNITION_CONFIDENCE ) {
//                        isWalkingStatus = true;
//                    }
                    confidence = confidence + activity.getConfidence();
                    Log.e( "ActivityRecognition", "On Foot: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.RUNNING: {
//                    if( activity.getConfidence() >= ACTIVITY_RECOGNITION_CONFIDENCE ) {
//                        isWalkingStatus = true;
//                    }
                    confidence = confidence + activity.getConfidence();
                    Log.e( "ActivityRecognition", "Running: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.WALKING: {
//                    if( activity.getConfidence() >= ACTIVITY_RECOGNITION_CONFIDENCE ) {
//                        isWalkingStatus = true;
//                    }
                    confidence = confidence + activity.getConfidence();
                    Log.e( "ActivityRecognition", "Walking: " + activity.getConfidence() );
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
}

