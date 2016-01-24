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
import android.util.Log;

import java.util.List;

public class AlertService extends Service implements SensorEventListener{

    public static final String ACTION = "Alert Service";
    private SensorManager mSensorManager;

    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    // 歩数計
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

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

        // 歩数計用のセンサー登録
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener (this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);




        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000,          // 10-second interval.
                10,             // 10 meters.
                listener);

        if (!gpsEnabled) {
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            // call enableLocationSettings()
        }
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // A new location update is received.  Do something useful with it.  In this case,
            // we're sending the update to a handler which then updates the UI with the new
            // location.
            Message.obtain(mHandler,
                    UPDATE_LATLNG,
                    location.getLatitude() + ", " +
                            location.getLongitude()).sendToTarget();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        mLocationManager.removeUpdates(listener);
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
        //　10回カウントして2秒ごとに下記を実行する
        if(mTendencyCheckCount == 10){
            // GPSで移動しているかどうかを判定
            // ある期間移動がなくなったらLocaitonManager remove
            // 再度歩行を検知したらまたLocationManager start


            int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
            //　下向きかどうかの判定
            // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
            // さらにテーブルに置いたときなど、水平状態があり得るため tendency > 3(適当) とする
            if ((tendency > 180 - MainActivity.sAlertStartAngle || tendency < MainActivity.sAlertStartAngle) && tendency > 3) {
                mTendencyOutCount++;
                //  下向きと判定されるのが連続5回の場合、Alertを表示させる
                if(mTendencyOutCount == 5) {
                    // 歩数計センサの利用：
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("isStepCounter",false);
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

        //　歩数計の値を取得
        if(MainActivity.sPedometerFlag) {
            Sensor sensor = event.sensor;
//            float[] values = event.values;
//            long timestamp = event.timestamp;
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                Intent intent = new Intent(ACTION);
                intent.putExtra("isStepCounter", true);
                sendBroadcast(intent);
            }
        }
    }
}

