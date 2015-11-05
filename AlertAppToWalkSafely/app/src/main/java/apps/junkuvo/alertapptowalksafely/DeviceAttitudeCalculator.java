package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class DeviceAttitudeCalculator {

    private final static double RAD2DEG = 180d/Math.PI;

    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];

//    float[] rotationMatrix = new float[9];
//    float[] rotationMatrixOut = new float[9];
//    float[] attitude = new float[3];

    public Utility mUtility;

    DeviceAttitudeCalculator(Context context){
        mUtility = new Utility(context);
    }

    public int calculateDeviceAttitude(SensorEvent event){
        // 値が変化したセンサーが照度センサーだった場合
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        }

        if (gravity != null){
            return calculateDeviceTendency(gravity);
        }

        /////  今回のアプリではワールド座標系等を考慮しない
        /////  単純に端末の重力のかかり方（傾き）だけを見る　→　Accelerometerのみ利用
//        if(geomagnetic != null && gravity != null) {
//            SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);
//
//            //　ワールド座標系から端末座標系への変換
//            //　★Activityの表示が縦固定の場合。横向きになる場合、修正が必要
//            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotationMatrixOut);
//            SensorManager.getOrientation(rotationMatrixOut, attitude);
//
//            // 逆立ちが90度となるのでキャスト後に-を付ける
//            return  -(int) (attitude[1] * RAD2DEG);
//        }

        // ★修正必要
        return 90;
    }

    // ★端末によって軸の向きが異なるようなので、対応が必要
    public int calculateDeviceTendency(float[] gravity){
        double tendencyDegree;
        double x;// 上下として定義（軸は向きによってx or z）
        double y;// 前後方向

        // スマホ縦　=　タブレット横
        boolean isTabletNotPhone = mUtility.isTabletNotPhone();
        int orientation = mUtility.getOrientation();

        if((isTabletNotPhone && orientation == Configuration.ORIENTATION_PORTRAIT)
                || (!isTabletNotPhone && orientation == Configuration.ORIENTATION_LANDSCAPE)){
            x = (double)gravity[0];
            y = (double)gravity[2];
        }else if((isTabletNotPhone && orientation == Configuration.ORIENTATION_LANDSCAPE)
                || (!isTabletNotPhone && orientation == Configuration.ORIENTATION_PORTRAIT)){
            x = (double)gravity[1];
            y = (double)gravity[2];
        }else{
            x = 9;
            y = 0;
        }

        double radian = Math.abs(Math.atan2(x, y));
        tendencyDegree = radian * RAD2DEG;
//        Log.d("test",Arrays.toString(gravity) + String.valueOf(tendencyDegree));

        return (int)tendencyDegree;
    }
}
