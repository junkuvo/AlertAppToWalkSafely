package apps.junkuvo.alertapptowalksafely;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private AlertService mAlertService;
    private final AlertReceiver mAlertReceiver = new AlertReceiver();
    public static boolean sAlertShowFlag = false;

    private class AlertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!sAlertShowFlag) {
                String alertMessage = ((EditText) findViewById(R.id.txtAlertMessage)).getText().toString();
                Toast toast = Toast.makeText(getApplicationContext(), alertMessage, Toast.LENGTH_SHORT);
                toast.show();
                sAlertShowFlag = true;
            }

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            long[] pattern = {1000, 1000, 1000, 1000}; // OFF/ON/OFF/ON...
//            vibrator.vibrate(pattern, -1);
            vibrator.vibrate(100);

//            int tendency = intent.getIntExtra("tendency",90);
//            TextView textview = (TextView)findViewById(R.id.txtTendency);
//            textview.setText(Integer.toString(tendency));
        }
    }

    // ServiceとActivityをBindするクラス
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mAlertService = ((AlertService.AlertBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mAlertService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button)findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // サービスを開始
                Intent intent = new Intent(MainActivity.this, AlertService.class);
                startService(intent);
                IntentFilter filter = new IntentFilter(AlertService.ACTION);

                registerReceiver(mAlertReceiver, filter);

                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });
    }

    // イベントリスナーの登録を解除
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection); // バインド解除
        unregisterReceiver(mAlertReceiver); // 登録解除
        mAlertService.stopSelf(); // サービスは必要ないので終了させる。
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_BACK:
//                finish();
//                moveTaskToBack(false);
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
