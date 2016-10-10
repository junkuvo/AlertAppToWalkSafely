package apps.junkuvo.alertapptowalksafely;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.stkent.amplify.prompt.BasePromptViewConfig;
import com.github.stkent.amplify.prompt.DefaultLayoutPromptView;
import com.github.stkent.amplify.tracking.Amplify;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.plus.PlusOneButton;
import com.growthbeat.Growthbeat;
import com.mhk.android.passcodeview.PasscodeView;
import com.software.shell.fab.ActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.fabric.sdk.android.Fabric;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final AlertReceiver mAlertReceiver = new AlertReceiver();
    public static boolean sAlertShowFlag = false;
    public static boolean sPedometerFlag = true;
    private boolean mAppRunningFlag = false;

    private Utility mUtility;

    public static int sAlertStartAngle;
    private boolean mVibrationOn = true;
    private boolean mPasscodeOn = false;
    private boolean mToastOn = true;
    private int mStepCount = 0;
    public static boolean mHasStepFeature = false;

    // SeekBarの最小値：0、最大値：60なので、実際の角度に対してはOFFSETが必要
    private final int ALERT_ANGLE_INITIAL_VALUE = 30;
    private final int ALERT_ANGLE_INITIAL_OFFSET = 15;

    private static final float TOAST_TEXT_SIZE = 32; // sp

    private static final int MENU_SETTING_ID = 0;
    private static final int MENU_SHARE_ID = 1;
    private static final int MENU_HISTORY_ID = 2;
    private static final String SETTING_SHAREDPREF_NAME = "setting";

    public static final String CLICK_NOTIFICATION = "click_notification";
    public static final String DELETE_NOTIFICATION = "delete_notification";

    private AlertDialog.Builder mAlertDialog;
    private EditText mTweetText;

    private SharedPreferencesUtil mSharedPreferenceUtil;

    private String mPasscode;
    private String mPasscodeConfirm;

    private ActionButton mbtnStart;
    private Animation mAnimationBlink;

    private PlusOneButton mPlusOneButton;

    private InterstitialAd mInterstitialAd;
    private static final String MY_AD_UNIT_ID = "ca-app-pub-1630604043812019/7857872217";

    private class AlertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("isStepCounter", false)) {
                if (sPedometerFlag) {
                    mStepCount = intent.getIntExtra("stepCount", mStepCount);
                    ((TextView) findViewById(R.id.txtStepCount)).setText(String.valueOf(mStepCount) + getString(R.string.home_step_count_dimension));
                }
            } else {
                if (!sAlertShowFlag && mToastOn) {
                    String alertMessage = ((EditText) findViewById(R.id.txtAlertMessage)).getText().toString();
                    createToastShort(alertMessage).show();
                    sAlertShowFlag = true;
                }

                if (mVibrationOn) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            long[] pattern = {1000, 1000, 1000, 1000}; // OFF/ON/OFF/ON...
//            vibrator.vibrate(pattern, -1);
                    vibrator.vibrate(100);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.show();
        }

        // 署名付きAPKではなぜか初期起動後、BGから起動される度にonCreateしてActivityを生み続ける
        // →Intentのフラグの値がおかしいらしいので、下記のコードで対応
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        // Create the interstitial.
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(MY_AD_UNIT_ID);

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("1BEC3806A9717F2A87F4D1FC2039D5F2")  // An device ID
                .build();

        // Begin loading your interstitial.
        mInterstitialAd.loadAd(adRequest);

        DefaultLayoutPromptView promptView = (DefaultLayoutPromptView) findViewById(R.id.prompt_view);

        final BasePromptViewConfig basePromptViewConfig
                = new BasePromptViewConfig.Builder()
                .setUserOpinionQuestionTitle(getString(R.string.prompt_title))
                .setUserOpinionQuestionPositiveButtonLabel(getString(R.string.prompt_btn_yes))
                .setUserOpinionQuestionNegativeButtonLabel(getString(R.string.prompt_btn_no))
                .setPositiveFeedbackQuestionTitle(getString(R.string.prompt_title_feedback))
                .setPositiveFeedbackQuestionPositiveButtonLabel(getString(R.string.prompt_btn_sure))
                .setPositiveFeedbackQuestionNegativeButtonLabel(getString(R.string.prompt_btn_notnow))
                .setCriticalFeedbackQuestionTitle(getString(R.string.prompt_title_feedback_2))
                .setCriticalFeedbackQuestionNegativeButtonLabel(getString(R.string.prompt_btn_notnow))
                .setCriticalFeedbackQuestionPositiveButtonLabel(getString(R.string.prompt_btn_sure))
                .setThanksTitle(getString(R.string.prompt_thanks))
                .build();

        promptView.applyBaseConfig(basePromptViewConfig);
        Amplify.getSharedInstance().promptIfReady(promptView);

        mUtility = new Utility(this);
        mAnimationBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        int buttonColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int buttonPressedColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        mbtnStart = (ActionButton) findViewById(R.id.fabStart);
        mbtnStart.setImageResource(R.drawable.ic_power_settings_new_white_48dp);
        mbtnStart.setButtonColor(buttonColor);
        mbtnStart.setButtonColorPressed(buttonPressedColor);

        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .enableIcon(true)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText(getString(R.string.intro_description))
                .setTarget(mbtnStart)
                .setUsageId(String.valueOf(mUtility.getVersionCode(this))) //THIS SHOULD BE UNIQUE ID
                .dismissOnTouch(true)
                .show();

        mbtnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                if (mPasscodeOn) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    final View layout = inflater.inflate(R.layout.passcode, (ViewGroup) findViewById(R.id.layout_root_passcode));
                    final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog(MainActivity.this)
                            .setTitle(getString(R.string.dialog_passcode_title))
                            .setDescription(getString(R.string.dialog_passcode_description))
                            .setIcon(R.drawable.ic_lock_blue_grey_100_48dp)
                            .setCustomView(layout).show();

//                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                    final PasscodeView passcodeView = (PasscodeView) layout.findViewById(R.id.passcode);
                    final PasscodeView passcodeViewConfirm = (PasscodeView) layout.findViewById(R.id.passcodeConfirm);
                    final TextView txtPasscodeConfirm = (TextView) layout.findViewById(R.id.txtPasscodeConfirm);
                    txtPasscodeConfirm.setText(getString(R.string.dialog_passcode_confirm));
                    ((TextView) layout.findViewById(R.id.txtPasscode)).setText(getString(R.string.dialog_passcode_passcode));
                    passcodeView.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
                        @Override
                        public void onPasscodeEntered(String passcode) {
                            if (mAppRunningFlag) {
                                if (passcode.equals(mPasscodeConfirm)) {
                                    FlurryAgent.logEvent("Passcode Unlocked");

                                    setStartButtonFunction(v);
                                    materialStyledDialog.dismiss();
                                } else {
                                    passcodeView.clearText();
                                    passcodeView.requestFocus();
                                }
                            } else {
                                passcodeViewConfirm.setVisibility(View.VISIBLE);
                                txtPasscodeConfirm.setVisibility(View.VISIBLE);
                                passcodeViewConfirm.requestFocus();
                                mPasscode = passcode;
                            }
                        }
                    });

                    passcodeViewConfirm.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
                        @Override
                        public void onPasscodeEntered(String passcode) {
                            mPasscodeConfirm = passcodeViewConfirm.getText().toString();
                            if (mPasscode.equals(mPasscodeConfirm)) {
                                FlurryAgent.logEvent("Passcode Lock");

                                setStartButtonFunction(v);
                                materialStyledDialog.dismiss();
                            } else {
                                passcodeView.clearText();
                                passcodeViewConfirm.clearText();
                                passcodeView.requestFocus();
                                passcodeViewConfirm.setVisibility(View.GONE);
                                txtPasscodeConfirm.setVisibility(View.GONE);
                            }
                        }
                    });

                } else {
                    setStartButtonFunction(v);
                }
            }
        });
        // スマホの場合はホーム画面自体は横にならないので縦に固定する(裏のロジックは横にも対応している)
        // タブレットはホームも縦横変化するのでこのアプリ画面も横に対応
        if (!mUtility.isTabletNotPhone()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        sAlertStartAngle = ALERT_ANGLE_INITIAL_VALUE + ALERT_ANGLE_INITIAL_OFFSET;
        mTwitter = TwitterUtility.getTwitterInstance(this);
        mCallbackURL = getString(R.string.twitter_callback_url);
        mSharedPreferenceUtil = new SharedPreferencesUtil();
        //　レビューサイトへのリンク
        setUrlLinkToReview();

        // 設定の読み込み
        SharedPreferences data = getSharedPreferences(SETTING_SHAREDPREF_NAME, Context.MODE_PRIVATE);
        mToastOn = data.getBoolean("message", true);
        mVibrationOn = data.getBoolean("vibrate", true);
        mPasscodeOn = data.getBoolean("passcode", false);
        sAlertStartAngle = data.getInt("progress", ALERT_ANGLE_INITIAL_VALUE) + ALERT_ANGLE_INITIAL_OFFSET;
        sPedometerFlag = data.getBoolean("pedometer", true);

        mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "VM7H7GMWZCFC496H4463");
        FlurryAgent.logEvent("onStart");

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rtlMain);
        relativeLayout.setOnClickListener(this);

        // カーソルを最後尾に移動
        EditText editText = (EditText) findViewById(R.id.txtAlertMessage);
        editText.setSelection(editText.getText().length());

        PackageManager packageManager = this.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)) {
            mHasStepFeature = true;
            if (sPedometerFlag) {
                ((TextView) findViewById(R.id.txtStepCount)).setVisibility(View.VISIBLE);
            } else {
                ((TextView) findViewById(R.id.txtStepCount)).setVisibility(View.INVISIBLE);
            }
        } else {
            mHasStepFeature = false;
        }

    }

    private static final int PLUS_ONE_REQUEST_CODE = 0;

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the state of the +1 button each time the activity receives focus.
        mPlusOneButton.initialize(String.format(getString(R.string.app_googlePlay_url_plusOne), getPackageName()), PLUS_ONE_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        FlurryAgent.logEvent("onClick aside from Start button");

        // キーボードを隠す
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        createToastShort(getString(R.string.toast_instruction)).show();
//        mbtnStart.setAnimation(mAnimationBlink);
        mbtnStart.startAnimation(mAnimationBlink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // メニューの要素を追加
        MenuItem actionItem = menu.add(Menu.NONE, MENU_SETTING_ID, MENU_SETTING_ID, this.getString(R.string.menu_title_setting));
        // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_manage);

        //  ★Twitter連携
        actionItem = menu.add(Menu.NONE, MENU_SHARE_ID, MENU_SHARE_ID, this.getString(R.string.menu_title_share));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(android.R.drawable.ic_menu_share);

//        //  ★歩数履歴
//        actionItem = menu.add(Menu.NONE,MENU_HISTORY_ID,MENU_HISTORY_ID,this.getString(R.string.menu_title_history));
//        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        actionItem.setIcon(android.R.drawable.ic_menu_recent_history);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View layout;
        switch (item.getItemId()) {
            case MENU_SETTING_ID:
                FlurryAgent.logEvent("Setting");
                // リスト表示用のアラートダイアログ
                layout = inflater.inflate(R.layout.setting, (ViewGroup) findViewById(R.id.layout_root));

                mAlertDialog = new AlertDialog.Builder(this);
                mAlertDialog.setTitle(this.getString(R.string.dialog_title_setting));
                mAlertDialog.setIcon(android.R.drawable.ic_menu_manage);
                mAlertDialog.setView(layout);
                mAlertDialog.setNegativeButton(this.getString(R.string.dialog_button_ok), null);

                setSeekBarInLayout(layout);
                setSwitchInLayout(layout);
                if (mHasStepFeature) {
                    setToggleButtonInLayout(layout);
                } else {
                    layout.findViewById(R.id.tgbPedometer).setVisibility(View.GONE);
                    layout.findViewById(R.id.txtPedometer).setVisibility(View.GONE);
                }
                mAlertDialog.create().show();
                break;
            case MENU_SHARE_ID:
                FlurryAgent.logEvent("Share twitter");
                if (!TwitterUtility.hasAccessToken(this)) {
                    startAuthorize();
                } else {
                    layout = inflater.inflate(R.layout.sharetotwitter, (ViewGroup) findViewById(R.id.layout_root_twitter));
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    mAlertDialog = new AlertDialog.Builder(this);
                    mAlertDialog.setTitle(this.getString(R.string.dialog_title_tweet));
                    mAlertDialog.setIcon(android.R.drawable.ic_menu_share);
                    mAlertDialog.setView(layout);
                    mTweetText = (EditText) layout.findViewById(R.id.edtTweet);
                    mTweetText.setText(getString(R.string.twitter_tweetText) + "\n" +
                            String.format(getString(R.string.app_googlePlay_url), getPackageName()) + "\n" + timeStamp);
                    mAlertDialog.setPositiveButton(this.getString(R.string.dialog_button_send), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tweet(mTweetText.getText().toString());
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.setNegativeButton(this.getString(R.string.dialog_button_cancel), null);
                    mAlertDialog.create().show();
                }
                break;
            case MENU_HISTORY_ID:
                layout = inflater.inflate(R.layout.history, (ViewGroup) findViewById(R.id.layout_root_history));
                setStepCountListInLayout(layout);
                mAlertDialog = new AlertDialog.Builder(this);
                mAlertDialog.setTitle(this.getString(R.string.dialog_title_history));
                mAlertDialog.setIcon(android.R.drawable.ic_menu_recent_history);
                mAlertDialog.setView(layout);
                mAlertDialog.setNegativeButton(this.getString(R.string.dialog_button_cancel), null);
                mAlertDialog.create().show();
                break;
        }
        return true;
    }

    public void setStartButtonFunction(View v) {
        DefaultLayoutPromptView promptView = (DefaultLayoutPromptView) findViewById(R.id.prompt_view);
        promptView.setVisibility(View.GONE);

        if (mAppRunningFlag) {
            FlurryAgent.logEvent("Service Stop!!");
            // サービス停止
            killAlertService();
            changeViewState(false, ((ActionButton) v));

            if (TwitterUtility.hasAccessToken(getApplicationContext())) {
                Context context = MainActivity.this;
                LayoutInflater inflater = LayoutInflater.from(context);
                View layout = inflater.inflate(R.layout.sharetotwitter, (ViewGroup) findViewById(R.id.layout_root_twitter));
                mAlertDialog = new AlertDialog.Builder(context);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                mAlertDialog.setTitle(context.getString(R.string.dialog_tweet));
                mAlertDialog.setIcon(android.R.drawable.ic_menu_share);
                mAlertDialog.setView(layout);
                mTweetText = (EditText) layout.findViewById(R.id.edtTweet);
                if(mHasStepFeature) {
                    mTweetText.setText(String.valueOf(mStepCount) + getString(R.string.twitter_tweet_step) + "\n" + getString(R.string.twitter_tweetText) + "\n" +
                            String.format(getString(R.string.app_googlePlay_url), getPackageName()) + "\n" + timeStamp);
                }else{
                    mTweetText.setText(getString(R.string.twitter_tweetText) + "\n" +
                            String.format(getString(R.string.app_googlePlay_url), getPackageName()) + "\n" + timeStamp);
                }
                mAlertDialog.setPositiveButton(context.getString(R.string.dialog_button_send), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tweet(mTweetText.getText().toString());
                    }
                });
                mAlertDialog.setCancelable(false);
                mAlertDialog.setNegativeButton(context.getString(R.string.dialog_button_cancel), null);
                mAlertDialog.create().show();
            }

            displayInterstitial();
        } else {
            FlurryAgent.logEvent("Service Start!!");
            // サービスを開始
            Intent intent = new Intent(MainActivity.this, AlertService.class);
            startService(intent);
            IntentFilter filter = new IntentFilter(AlertService.ACTION);
            registerReceiver(mAlertReceiver, filter);

//            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            changeViewState(true, ((ActionButton) v));
            mStepCount = 0;
        }
    }

    public void setStepCountListInLayout(View layout) {
//        ListView listView = (ListView)layout.findViewById(R.id.livStepCountHistory);
    }

    public void setSeekBarInLayout(View layout) {
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.skbSensitivity);
        seekBar.setProgress(sAlertStartAngle - ALERT_ANGLE_INITIAL_OFFSET);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        FlurryAgent.logEvent("Setting seekbar");
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

    public void setSwitchInLayout(View layout) {
        Switch swh = (Switch) layout.findViewById(R.id.swhToastOnOff);
        swh.setChecked(mToastOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mToastOn = isChecked;
                    }
                }
        );
        swh = (Switch) layout.findViewById(R.id.swhVibrationOnOff);
        swh.setChecked(mVibrationOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mVibrationOn = isChecked;
                    }
                }
        );
        swh = (Switch) layout.findViewById(R.id.swhPasscodeOnOff);
        swh.setChecked(mPasscodeOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mPasscodeOn = isChecked;
                    }
                }
        );

        if (mAppRunningFlag) {
            swh.setEnabled(false);
        } else {
            swh.setEnabled(true);
        }
    }

    public void setToggleButtonInLayout(View layout) {
        ToggleButton tgbPedometer = (ToggleButton) layout.findViewById(R.id.tgbPedometer);
        tgbPedometer.setChecked(sPedometerFlag);
        tgbPedometer.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sPedometerFlag = isChecked;
                        if (isChecked) {
                            ((TextView) findViewById(R.id.txtStepCount)).setVisibility(View.VISIBLE);
                        } else {
                            ((TextView) findViewById(R.id.txtStepCount)).setVisibility(View.INVISIBLE);
                        }
                    }
                }
        );
    }

    public void setUrlLinkToReview() {
        SpannableString content = new SpannableString(getString(R.string.review_url_title));
        content.setSpan(new UnderlineSpan(), 0, getString(R.string.review_url_title).length(), 0);
        TextView tv = (TextView) findViewById(R.id.home);
        tv.setText(content);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(String.format(getApplicationContext().getString(R.string.review_url), getApplicationContext().getPackageName()));
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException activityNotFound) {
                    // to handle play store not installed scenario
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                    startActivity(intent);
                }
            }
        });
    }

    // イベントリスナーの登録を解除
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences data = getSharedPreferences(SETTING_SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean("message", mToastOn);
        editor.putBoolean("vibrate", mVibrationOn);
        editor.putBoolean("passcode", mPasscodeOn);
        editor.putInt("progress", sAlertStartAngle - ALERT_ANGLE_INITIAL_OFFSET);
        editor.putBoolean("pedometer", sPedometerFlag);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAppRunningFlag) {
            killAlertService();
        }

        Growthbeat.getInstance().stop();
    }

    public void killAlertService() {
        unregisterReceiver(mAlertReceiver); // 登録解除
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//                finish();
                // 端末のホーム画面に戻る
                moveTaskToBack(true);
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changeViewState(boolean isStart, ActionButton button) {
        if (isStart) {
//            button.setText(this.getString(R.string.home_button_stop));
            button.setImageResource(R.drawable.ic_done_white_48dp);
            button.setButtonColor(ContextCompat.getColor(this, R.color.colorAccent));
            button.setButtonColorPressed(ContextCompat.getColor(this, R.color.colorAccentDark));
//            button.setBackgroundResource(R.drawable.shape_rounded_corners_red_5dp);
            mAppRunningFlag = true;
            ((TextView) findViewById(R.id.txtStepCount)).setText("0" + getString(R.string.home_step_count_dimension));
        } else {
//            button.setBackgroundResource(R.drawable.shape_rounded_corners_blue_5dp);
//            button.setText(this.getString(R.string.home_button_start));
            button.setImageResource(R.drawable.ic_power_settings_new_white_48dp);
            button.setButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
            button.setButtonColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark));
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
                    // OAuth用のURLを作成
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
                    createToastShort(getApplicationContext().getString(R.string.twitter_auth_succeed)).show();
                    TwitterUtility.storeAccessToken(getApplicationContext(), accessToken);
                } else {
                    createToastShort(getApplicationContext().getString(R.string.twitter_auth_fail)).show();
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
                    FlurryAgent.logEvent(getApplicationContext().getString(R.string.twitter_tweet_succeed));
                    createToastShort(getApplicationContext().getString(R.string.twitter_tweet_succeed)).show();
                } else {
                    createToastShort(getApplicationContext().getString(R.string.twitter_tweet_fail)).show();
                }
            }
        };
        task.execute(tweetString);
    }

    private Toast createToastShort(String text) {
        Toast toast = new Toast(this);
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TOAST_TEXT_SIZE);
        toast.setView(tv);
        toast.setDuration(Toast.LENGTH_SHORT);

        return toast;
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public static boolean issAlertShowFlag() {
        return sAlertShowFlag;
    }

    public static void setsAlertShowFlag(boolean sAlertShowFlag) {
        MainActivity.sAlertShowFlag = sAlertShowFlag;
    }

    public static boolean issPedometerFlag() {
        return sPedometerFlag;
    }

    public static void setsPedometerFlag(boolean sPedometerFlag) {
        MainActivity.sPedometerFlag = sPedometerFlag;
    }
}
