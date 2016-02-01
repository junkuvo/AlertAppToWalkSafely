package apps.junkuvo.lunchtimelog;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationDetectService extends Service implements SensorEventListener {

    public static final String ACTION = "Location Detect Service";
    private SensorManager mSensorManager;
    private int mStepCount = 0;

    // 歩数計
    private Sensor mStepDetectorSensor;
    private Sensor mStepCounterSensor;

    // Location
    Location mBestLocation = null;
    LocationManager mLocationManager;
    // Define a listener that responds to location updates
    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("test",location.getProvider());
            if(mBestLocation == null) {
                mBestLocation = location;
            }else{
                if(mLocationUtility.isBetterLocation(location, mBestLocation)){
                    mBestLocation = location;
                }
            }

            Intent intent = new Intent(ACTION);
            intent.putExtra("latitude", mBestLocation.getLatitude());
            intent.putExtra("longitude", mBestLocation.getLongitude());
            sendBroadcast(intent);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };
    LocationUtility mLocationUtility;

    @Override
    public void onCreate() {
        super.onCreate();
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationUtility = new LocationUtility();
        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can also request location updates from both the GPS and the Network Location Provider by calling requestLocationUpdates()
            // twice—once for NETWORK_PROVIDER and once for GPS_PROVIDER.
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 歩数計用のセンサー登録
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        // Remove the listener you previously added
        if (mLocationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.removeUpdates(mLocationListener);
            }
        }
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
        return new LocationDetectBinder();
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
    }

    class LocationDetectBinder extends Binder {
        LocationDetectService getService() {
            return LocationDetectService.this;
        }
    }

    // センサーの精度が変更されると呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // センサーの値が変化すると呼ばれる
    @Override
    public void onSensorChanged(SensorEvent event) {

        //　歩数計の値を取得
        Sensor sensor = event.sensor;
//            float[] values = event.values;
//            long timestamp = event.timestamp;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            mStepCount++;//Integer.valueOf(String.valueOf(values[0]));
            if(MainActivity.sPedometerFlag) {
                Intent intent = new Intent(ACTION);
                intent.putExtra("isStepCounter", true);
                intent.putExtra("stepCount",mStepCount);
                sendBroadcast(intent);
            }
        }
    }
}

