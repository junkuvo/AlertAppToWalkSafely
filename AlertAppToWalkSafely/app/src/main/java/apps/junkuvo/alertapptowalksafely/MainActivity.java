package apps.junkuvo.alertapptowalksafely;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends ActionBarActivity {

    private AlertService mAlertService;
    private final AlertReceiver mAlertReceiver = new AlertReceiver();
    public static boolean sAlertShowFlag = false;
    private boolean mAppRunningFlag = false;

    private Utility mUtility;

    public static int sAlertStartAngle;
    public boolean mVibrationOn = true;
    public boolean mToastOn = true;

    // SeekBarの最小値：0、最大値：60なので、実際の角度に対してはOFFSETが必要
    private final int ALERT_ANGLE_INITIAL_VALUE = 30;
    private final int ALERT_ANGLE_INITIAL_OFFSET = 15;

    private static final int MENU_SETTING_ID = 0;
    private static final int MENU_SHARE_ID = 1;

    private AlertDialog.Builder mAlertDialog;
    private EditText mTweetText;

    private class AlertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!sAlertShowFlag && mToastOn) {
                String alertMessage = ((EditText) findViewById(R.id.txtAlertMessage)).getText().toString();
                showToastShort(alertMessage);
                sAlertShowFlag = true;
            }

            if(mVibrationOn) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            long[] pattern = {1000, 1000, 1000, 1000}; // OFF/ON/OFF/ON...
//            vibrator.vibrate(pattern, -1);
                vibrator.vibrate(100);
            }
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

        ActionBar actionbar = getSupportActionBar();
        actionbar.show();
        // 署名付きAPKではなぜか初期起動後、BGから起動される度にonCreateしてActivityを生み続ける
        // →Intentのフラグの値がおかしいらしいので、下記のコードで対応
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        Button startButton = (Button)findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mAppRunningFlag) {
                    killAlertService();
                    changeViewState(false, ((Button) v));
                } else {
                    // サービスを開始
                    Intent intent = new Intent(MainActivity.this, AlertService.class);
                    startService(intent);
                    IntentFilter filter = new IntentFilter(AlertService.ACTION);
                    registerReceiver(mAlertReceiver, filter);

                    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    changeViewState(true,((Button)v));
                }
            }
        });
        mUtility = new Utility(this);
        // スマホの場合はホーム画面自体は横にならないので縦に固定する(裏のロジックは横にも対応している)
        // タブレットはホームも縦横変化するのでこのアプリ画面も横に対応
        if(!mUtility.isTabletNotPhone()){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        sAlertStartAngle = ALERT_ANGLE_INITIAL_VALUE + ALERT_ANGLE_INITIAL_OFFSET;
        mTwitter = TwitterUtility.getTwitterInstance(this);
        mCallbackURL = getString(R.string.twitter_callback_url);

        //　レビューサイトへのリンク
        setUrlLinkToReview();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // メニューの要素を追加
        MenuItem actionItem = menu.add(Menu.NONE,MENU_SETTING_ID,MENU_SETTING_ID,"設定");
        // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_manage);

          //  ★FBやTwitter連携
        actionItem = menu.add(Menu.NONE,MENU_SHARE_ID,MENU_SHARE_ID,"共有");
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(android.R.drawable.ic_menu_share);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View layout;
        switch(item.getItemId()){
            case MENU_SETTING_ID :
                // リスト表示用のアラートダイアログ
                layout = inflater.inflate(R.layout.setting, (ViewGroup) findViewById(R.id.layout_root));

                mAlertDialog = new AlertDialog.Builder(this);
                mAlertDialog.setTitle("各種設定");
                mAlertDialog.setIcon(android.R.drawable.ic_menu_manage);
                mAlertDialog.setView(layout);

                setSeekBarInLayout(layout);
                setSwitchInLayout(layout);
                mAlertDialog.create().show();
                break;
            case MENU_SHARE_ID :
                if (!TwitterUtility.hasAccessToken(this)) {
                    startAuthorize();
                }else{
                    layout = inflater.inflate(R.layout.sharetotwitter, (ViewGroup) findViewById(R.id.layout_root_twitter));
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    mAlertDialog = new AlertDialog.Builder(this);
                    mAlertDialog.setTitle("つぶやく");
                    mAlertDialog.setIcon(android.R.drawable.ic_menu_share);
                    mAlertDialog.setView(layout);
                    mTweetText = (EditText)layout.findViewById(R.id.edtTweet);
                    mTweetText.setText(getString(R.string.twitter_tweetText) + "\n" +
                            String.format(getString(R.string.app_googlePlay_url),getPackageName()) + "\n" + timeStamp);
                    mAlertDialog.setPositiveButton("送信", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tweet(mTweetText.getText().toString());
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.setNegativeButton("キャンセル", null);
                    mAlertDialog.create().show();
                }
                break;
        }
        return true;
    }

    public void setSeekBarInLayout(View layout){
        SeekBar seekBar = (SeekBar)layout.findViewById(R.id.skbSensitivity);
        seekBar.setProgress(sAlertStartAngle - ALERT_ANGLE_INITIAL_OFFSET);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        sAlertStartAngle = progress + ALERT_ANGLE_INITIAL_OFFSET;
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );
    }

    public void setSwitchInLayout(View layout){
        Switch swh = (Switch)layout.findViewById(R.id.swhToastOnOff);
        swh.setChecked(mToastOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mToastOn = isChecked;
                    }
                }
        );
        swh = (Switch)layout.findViewById(R.id.swhVibrationOnOff);
        swh.setChecked(mVibrationOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mVibrationOn = isChecked;
                    }
                }
        );
    }

    public void setUrlLinkToReview(){
        SpannableString content = new SpannableString(getString(R.string.review_url_title));
        content.setSpan(new UnderlineSpan(), 0, getString(R.string.review_url_title).length(), 0);
        TextView tv = (TextView) findViewById(R.id.home);
        tv.setText(content);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(String.format(getApplicationContext().getString(R.string.review_url),getApplicationContext().getPackageName()));
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
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
        if(mAppRunningFlag) {
            killAlertService();
        }
    }

    public void killAlertService(){
        unbindService(mServiceConnection); // バインド解除
        unregisterReceiver(mAlertReceiver); // 登録解除
        mAlertService.stopSelf(); // サービスは必要ないので終了させる。
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

    public void changeViewState(boolean isStart,Button button){
        if(isStart){
            ((Button) button).setText("停止");
            ((Button) button).setBackgroundResource(R.drawable.shape_rounded_corners_red_5dp);
            mAppRunningFlag = true;
        }else{
            ((Button) button).setText("開始");
            ((Button) button).setBackgroundResource(R.drawable.shape_rounded_corners_blue_5dp);
            mAppRunningFlag = false;
            MainActivity.sAlertShowFlag = false;
        }
    }

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    /**
     * OAuth認証（厳密には認可）を開始します。
     */
    private void startAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mTwitter.setOAuthAccessToken(null);// これがないと認証画面に1回しか飛べない
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                }
            }
        };
        task.execute();
    }


    @Override
    public void onNewIntent(Intent intent) {
        if (intent == null || intent.getData() == null || !intent.getData().toString().startsWith(mCallbackURL)) {
            return;
        }
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    showToastShort("Twitter認証が完了しました！");
                    TwitterUtility.storeAccessToken(getApplicationContext(), accessToken);
                } else {
                    showToastShort("Twitter認証に失敗しました。。。");
                }
            }
        };
        task.execute(verifier);
    }

    private void tweet(String tweetString) {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    mTwitter.updateStatus(params[0]);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    showToastShort("ツイートが完了しました！");
                } else {
                    showToastShort("ツイートに失敗しました。。。");
                }
            }
        };
        task.execute(tweetString);
    }

    private void showToastShort(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
