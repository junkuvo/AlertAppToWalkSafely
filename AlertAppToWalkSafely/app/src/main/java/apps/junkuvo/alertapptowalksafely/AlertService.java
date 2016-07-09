package apps.junkuvo.alertapptowalksafely;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class AlertService extends Service implements SensorEventListener {

    public static final String ACTION = "Alert Service";
    private SensorManager mSensorManager;

    // 歩きスマホの判定に利用（姿勢の計算）
    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    // 歩数カウントに利用（ステップセンサがない場合）
    private WalkCountCalculator mWalkCountCalculator;

    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

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

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(getApplicationContext());
        mWalkCountCalculator = new WalkCountCalculator();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStatusReceiver, filter);
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
        if(mIsScreenOn) {
            mTendencyCheckCount++;
            if (mStepCountBefore == 0) {
                mStepCountBefore = mStepCountCurrent;
            }
            // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
            // 5回カウントして2秒ごとに下記を実行する(1秒くらいあれば歩数が変化している前提)
            if (mTendencyCheckCount == 5) {
                // 歩行中であることを判定
                // 歩行センサーがない場合は角度mTendencyCheckCountのみ
                if (isWalking()) {
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
                        }
                    } else {
                        if (mTendencyOutCount > 0) {
                            mTendencyOutCount--;
                            MainActivity.sAlertShowFlag = false;
                        }
                    }
                }
                mTendencyCheckCount = 0;
            }
        }

        // 歩数計の値を取得
        Sensor sensor = event.sensor;
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
            // 歩行センサがない場合
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
}

