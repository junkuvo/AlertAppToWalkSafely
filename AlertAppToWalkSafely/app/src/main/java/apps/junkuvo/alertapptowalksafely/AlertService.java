package apps.junkuvo.alertapptowalksafely;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class AlertService extends Service implements SensorEventListener{

    public static final String ACTION = "Alert Service";
    private SensorManager mSensorManager;

    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast toast = Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT);
//        toast.show();
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
//        Toast toast = Toast.makeText(getApplicationContext(), "onStart()", Toast.LENGTH_SHORT);
//        toast.show();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // センサーのオブジェクトリストを取得する
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
        sensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast toast = Toast.makeText(getApplicationContext(), "onsDestroy()", Toast.LENGTH_SHORT);
//        toast.show();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
//        Toast toast = Toast.makeText(getApplicationContext(), "onBind()", Toast.LENGTH_SHORT);
//        toast.show();
        return new AlertBinder();
    }

    @Override
    public void onRebind(Intent intent) {
//        Toast toast = Toast.makeText(getApplicationContext(), "onRebind()", Toast.LENGTH_SHORT);
//        toast.show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        Toast toast = Toast.makeText(getApplicationContext(), "onUnbind()", Toast.LENGTH_SHORT);
//        toast.show();
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

    // センサーの値が変化すると呼ばれる
    @Override
    public void onSensorChanged(SensorEvent event) {
        mTendencyCheckCount++;
        // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
        //　5回カウントして1秒ごとに下記を実行する
        if(mTendencyCheckCount == 5){
            int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
            //　下向きかどうかの判定
            // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
            // さらにテーブルに置いたときなど、水平状態があり得るため tendency > 3(適当) とする
            if ((tendency > 180 - MainActivity.sAlertStartAngle || tendency < MainActivity.sAlertStartAngle) && tendency > 3) {
                mTendencyOutCount++;
                //  下向きと判定されるのが連続5回の場合、Alertを表示させる
                if(mTendencyOutCount == 5) {
                    Intent intent = new Intent(ACTION);
                    sendBroadcast(intent);
                    mTendencyOutCount = 0;
                }
            } else {
                if(mTendencyOutCount > 0) {
                    mTendencyOutCount--;
                    MainActivity.sAlertShowFlag = false;
                }
            }
            mTendencyCheckCount = 0;
        }
    }
}

