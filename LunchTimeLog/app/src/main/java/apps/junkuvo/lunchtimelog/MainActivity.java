package apps.junkuvo.lunchtimelog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    private boolean mAppRunningFlag = false;
    public static boolean sPedometerFlag = true;
    private LocationDetectService mLocationDetectService;
    private final LocationDetectReceiver mLocationDetectReceiver = new LocationDetectReceiver();
    // ServiceとActivityをBindするクラス
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mLocationDetectService = ((LocationDetectService.LocationDetectBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mLocationDetectService = null;
        }
    };

    private double mLatitude = 0.0f;
    private double mLongitude = 0.0f;
    private class LocationDetectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLng latLng = new LatLng(intent.getDoubleExtra("latitude",mLatitude), intent.getDoubleExtra("longitude",mLongitude));
            mLatitude = intent.getDoubleExtra("latitude",mLatitude);
            mLongitude = intent.getDoubleExtra("longitude",mLongitude);
            mMapsUtility.setCurrentLocationMarker(latLng);
        }
    }

    private AlertDialog.Builder mAlertDialog;
    private MapsUtility mMapsUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 署名付きAPKではなぜか初期起動後、BGから起動される度にonCreateしてActivityを生み続ける
        // →Intentのフラグの値がおかしいらしいので、下記のコードで対応
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        mMapsUtility = new MapsUtility(this);
        mMapsUtility.setMap(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap());

        Button startButton = (Button)findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mAppRunningFlag) {
                    // サービス停止
                    killLocationDetectService();
                    changeViewState(false, ((Button) v));

                } else {
                    // サービスを開始
                    Intent intent = new Intent(MainActivity.this, LocationDetectService.class);
                    startService(intent);
                    IntentFilter filter = new IntentFilter(LocationDetectService.ACTION);
                    registerReceiver(mLocationDetectReceiver, filter);

                    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    changeViewState(true, ((Button) v));
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAppRunningFlag) {
            killLocationDetectService();
        }
    }

    public void killLocationDetectService(){
        unbindService(mServiceConnection); // バインド解除
        unregisterReceiver(mLocationDetectReceiver); // 登録解除
        mLocationDetectService.stopSelf(); // サービスは必要ないので終了させる。
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
//                finish();
                // 端末のホーム画面に戻る
                moveTaskToBack(true);
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeViewState(boolean isStart,Button button) {
        if (isStart) {
            button.setText(this.getString(R.string.home_button_stop));
            button.setBackgroundResource(R.drawable.shape_rounded_corners_red_5dp);
            mAppRunningFlag = true;
//            ((TextView)findViewById(R.id.txtStepCount)).setText("0"+ getString(R.string.home_step_count_dimension));
        } else {
            button.setBackgroundResource(R.drawable.shape_rounded_corners_blue_5dp);
            button.setText(this.getString(R.string.home_button_start));
            mAppRunningFlag = false;
        }
    }
}
