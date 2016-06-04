package apps.junkuvo.gravitysensortest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.stkent.amplify.tracking.AmplifyStateTracker;
import com.github.stkent.amplify.views.AmplifyView;


public class MainActivity extends Activity
        implements SensorEventListener {

    protected final static double RAD2DEG = 180/Math.PI;

    SensorManager sensorManager;

    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];

    TextView azimuthText;
    TextView pitchText;
    TextView rollText;

    TextView geo1;
    TextView geo2;
    TextView geo3;

    TextView gra1;
    TextView gra2;
    TextView gra3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast toast = Toast.makeText(getApplicationContext(), "onCreate()" , Toast.LENGTH_SHORT);
        toast.show();
        findViews();
        initSensor();
    }

    public void onResume(){
        super.onResume();
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause(){
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

    protected void findViews(){
        azimuthText = (TextView)findViewById(R.id.txtYou);
        pitchText = (TextView)findViewById(R.id.txtPitch);
        rollText = (TextView)findViewById(R.id.txtRoll);

        geo1 = (TextView)findViewById(R.id.txtGeo1);
        geo2 = (TextView)findViewById(R.id.txtGeo2);
        geo3 = (TextView)findViewById(R.id.txtGeo3);

        gra1 = (TextView)findViewById(R.id.txtGra1);
        gra2 = (TextView)findViewById(R.id.txtGra2);
        gra3 = (TextView)findViewById(R.id.txtGra3);
    }

    protected void initSensor(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }

        if(geomagnetic != null && gravity != null){
            SensorManager.getRotationMatrix(
                    rotationMatrix, null,
                    gravity, geomagnetic);

            SensorManager.getOrientation(
                    rotationMatrix,
                    attitude);

            azimuthText.setText(Integer.toString((int)(attitude[0] * RAD2DEG)));
            pitchText.setText(Integer.toString((int) (attitude[1] * RAD2DEG)));
            rollText.setText(Integer.toString((int) (attitude[2] * RAD2DEG)));

            gra1.setText(Float.toString((gravity[0])));
            gra2.setText(Float.toString( (gravity[1] )));
            gra3.setText(Float.toString((gravity[2] )));

            geo1.setText(Float.toString( (geomagnetic[0] )));
            geo2.setText(Float.toString( (geomagnetic[1] )));
            geo3.setText(Float.toString( (geomagnetic[2] )));

        }

    }
}