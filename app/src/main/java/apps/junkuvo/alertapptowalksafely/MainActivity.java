package apps.junkuvo.alertapptowalksafely;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.LikeView;
import com.flurry.android.FlurryAgent;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.stkent.amplify.prompt.BasePromptViewConfig;
import com.github.stkent.amplify.prompt.DefaultLayoutPromptView;
import com.github.stkent.amplify.tracking.Amplify;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.plus.PlusOneButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.growthbeat.Growthbeat;
import com.mhk.android.passcodeview.PasscodeView;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.software.shell.fab.ActionButton;
import com.webianks.easy_feedback.EasyFeedback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;
import apps.junkuvo.alertapptowalksafely.utils.DateUtil;
import apps.junkuvo.alertapptowalksafely.utils.LINEUtil;
import apps.junkuvo.alertapptowalksafely.utils.RealmUtil;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.fabric.sdk.android.Fabric;
import junkuvo.apps.androidutility.SharedPreferencesUtil;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;

public class MainActivity extends AbstractActivity implements View.OnClickListener {

    private boolean mPasscodeOn = false;
    private int mStepCount = 0;
    public static final String SETTING_SHAREDPREF_NAME = "setting";

    // SeekBarの最小値：0、最大値：60なので、実際の角度に対してはOFFSETが必要
    private final int ALERT_ANGLE_INITIAL_VALUE = 30;
    private final int ALERT_ANGLE_INITIAL_OFFSET = 15;

    private static final float TOAST_TEXT_SIZE = 32; // sp

    /**
     * メニュー表示順に並べること
     */
    enum MENU_ID {
        HISTORY(R.drawable.ic_history_white_24dp),
        FACEBOOK(R.drawable.ic_action_fb_f_logo__white_1024),
        TWITTER(R.drawable.ic_action_twitter_logo_white_on_image),
        LINE(R.drawable.ic_action_fb_f_logo__white_1024),
        FEEDBACK(0),
        SETTING(R.drawable.ic_settings_white_24dp),
        LICENSE(0);

        private int drawableResId;

        MENU_ID(@DrawableRes int drawableResId) {
            this.drawableResId = drawableResId;
        }

        public int getDrawableResId() {
            return drawableResId;
        }

        public static MENU_ID getMenuIdEnum(int itemId) {
            for (int i = 0; i < MENU_ID.values().length; i++) {
                if (MENU_ID.values()[i].ordinal() == itemId) {
                    return MENU_ID.values()[i];
                }
            }
            return null;
        }
    }

    private AlertDialog.Builder mAlertDialog;
    private EditText mTweetText;

    private String mPasscode;
    private String mPasscodeConfirm;

    private ActionButton mbtnStart;
    private Animation mAnimationBlink;
    private EditText mAlertEditText;

    private PlusOneButton mPlusOneButton;

    private InterstitialAd mInterstitialAd;

    @VisibleForTesting
    boolean mShouldShowPedometer = true;
    @VisibleForTesting
    boolean mIsToastOn = true;
    @VisibleForTesting
    boolean mIsVibrationOn = true;
    @VisibleForTesting
    int mToastPosition;
    @VisibleForTesting
    int mAlertStartAngle;

    private String mAlertMessage;

    public TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAlertMessage = s.toString();
            if (mAlertService != null && mAlertService.isBoundService()) {
                mAlertService.setAlertMessage(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // ActivityがonDestroyしたあと、ここでBindされるServiceは同一（Rebindされる）
    private AlertService mAlertService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // サービスにはIBinder経由で#getService()してダイレクトにアクセス可能
            mAlertService = ((AlertService.AlertServiceBinder) binder).getService();
            mAlertService.setOnWalkStepListener(new AlertService.onWalkStepListener() {
                @Override
                public void onAlertWalkStep(int stepCount) {
                    ((TextView) findViewById(R.id.txtStepCount)).setText(String.valueOf(stepCount) + getString(R.string.home_step_count_dimension));
                }

                @Override
                public void onWalkStep(int stepCount) {
                    ((TextView) findViewById(R.id.txtStepNo)).setText(String.valueOf(stepCount) + getString(R.string.home_step_count_dimension));
                }
            });
            mAlertService.setOnActionFromNotificationListener(new AlertService.onActionFromNotificationListener() {
                @Override
                public void onStopFromNotification(String action) {
                    mAlertService.setIsRunningAlertService(true);
                    setStartButtonFunction(findViewById(R.id.fabStart));
                }
            });
            mAlertService.setOverlayActionListener(new AlertService.OverlayActionListener() {
                @Override
                public void onOpenApp() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(intent);
                }
            });
            mAlertService.setIsToastOn(mIsToastOn);
            mAlertService.setIsVibrationOn(mIsVibrationOn);
            mAlertService.setToastPosition(mToastPosition);
            mAlertService.setAlertStartAngle(mAlertStartAngle + ALERT_ANGLE_INITIAL_OFFSET);
            mAlertService.setAlertMessage(mAlertMessage);
            mAlertService.setIsBoundService(true);
            String initial = getString(R.string.zero) + getString(R.string.home_step_count_dimension);
            ((TextView) findViewById(R.id.txtStepCount)).setText(initial);

            if (mAlertService.IsRunningAlertService()) {
                // ボタン等の状態を合わせるため、falseにしてsetStartButtonFunctionを呼ぶ
                mAlertService.setIsRunningAlertService(false);
                setStartButtonFunction(findViewById(R.id.fabStart));
            } else {
                mAlertService.setStepCountCurrent(0);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // サービスとの切断(異常系処理)
            // プロセスのクラッシュなど意図しないサービスの切断が発生した場合に呼ばれる。
            // Unbindのタイミングでは呼ばれません（別物）
            mAlertService = null;
        }
    };

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

        if (!isRunningJunit) {
            setContentView(R.layout.activity_main);

            FirebaseRemoteConfigUtil.initialize();

            LikeView likeView = (LikeView) findViewById(R.id.like_view);
            likeView.setObjectIdAndType(String.format(getString(R.string.app_googlePlay_url), getPackageName()), LikeView.ObjectType.PAGE);

            // Create the interstitial.
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.ad_mob_id));

            // Create ad request.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice("1BEC3806A9717F2A87F4D1FC2039D5F2")  // An device ID ASUS
                    .addTestDevice("64D37FCE47B679A7F4639D180EC4C547")
                    .build();

            // Begin loading your interstitial.
            mInterstitialAd.loadAd(adRequest);

            // FIXME : クリック率悪いので消します。
            AdView mAdView = (AdView) findViewById(R.id.adView);
            mAdView.loadAd(adRequest);

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

            Utility mUtility = new Utility(this);
            mAnimationBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

            int buttonColor = ContextCompat.getColor(this, R.color.colorPrimary);
            int buttonPressedColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
            mbtnStart = (ActionButton) findViewById(R.id.fabStart);
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


            mbtnStart.setOnClickListener(this);

            Intent mAlertServiceIntent = new Intent(MainActivity.this, AlertService.class);

            // スマホの場合はホーム画面自体は横にならないので縦に固定する(裏のロジックは横にも対応している)
            // タブレットはホームも縦横変化するのでこのアプリ画面も横に対応
            if (!Utility.isTabletNotPhone(this)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mTwitter = TwitterUtility.getTwitterInstance(this);
            mCallbackURL = getString(R.string.twitter_callback_url);

            mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
            // Refresh the state of the +1 button each time the activity receives focus.
            mPlusOneButton.initialize(String.format(getString(R.string.app_googlePlay_url_plusOne), getPackageName()), PLUS_ONE_REQUEST_CODE);

            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rtlMain);
            relativeLayout.setOnClickListener(this);

            findViewById(R.id.llStepCount).setVisibility(mShouldShowPedometer ? View.VISIBLE : View.INVISIBLE);

//        // Serviceが動いていてもActivityがDestroyされた場合にActivityを再起動するとき、
//        // UIとServiceの状況を合わせるためにServiceの動きを把握する必要があるが、Bindができないので
//        // Application変数を参照している
//        if (((AlertApplication) getApplication()).IsRunningService()) {
//            btnIsStarted = false;
//            setStartButtonFunction(findViewById(R.id.fabStart));
//        }
            // サービスを開始
//            startService(mAlertServiceIntent);
            bindService(mAlertServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        }

        mIsToastOn = SharedPreferencesUtil.getBoolean(this, SETTING_SHAREDPREF_NAME, "message", true);
        mIsVibrationOn = SharedPreferencesUtil.getBoolean(this, SETTING_SHAREDPREF_NAME, "vibrate", true);
        mToastPosition = SharedPreferencesUtil.getInt(this, SETTING_SHAREDPREF_NAME, "toastPosition", Gravity.CENTER);
        mAlertStartAngle = SharedPreferencesUtil.getInt(this, SETTING_SHAREDPREF_NAME, "progress", ALERT_ANGLE_INITIAL_VALUE) + ALERT_ANGLE_INITIAL_OFFSET;
        mShouldShowPedometer = SharedPreferencesUtil.getBoolean(this, SETTING_SHAREDPREF_NAME, "pedometer", true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isRunningJunit) {
            // カーソルを最後尾に移動
            mAlertEditText = (EditText) findViewById(R.id.txtAlertMessage);
            mAlertEditText.setSelection(mAlertEditText.getText().length());
            mAlertEditText.addTextChangedListener(mTextWatcher);
            mAlertMessage = mAlertEditText.getText().toString();
        }
    }

    private static final int PLUS_ONE_REQUEST_CODE = 0;

    @Override
    protected void onResume() {
        super.onResume();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_session_id));
        FlurryAgent.logEvent("onResume");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rtlMain:

//                long cacheExpirationSeconds = BuildConfig.DEBUG ? 10 : 60 * 60;// Cache時間設定(秒)
//                FirebaseRemoteConfigUtil.fetch(cacheExpirationSeconds);
//                Toast.makeText(getApplicationContext(), FirebaseRemoteConfig.getInstance().getString("test_string"), Toast.LENGTH_SHORT).show();

                FlurryAgent.logEvent("onClick aside from Start button");

                // キーボードを隠す
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (!mAlertService.IsRunningAlertService()) {
                    createToastShort(getString(R.string.toast_instruction)).show();
                    mbtnStart.startAnimation(mAnimationBlink);
                }
                break;
            case R.id.fabStart:
                startBtnClick(v);
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // メニューの要素を追加
        MenuItem actionItem = menu.add(Menu.NONE, MENU_ID.SETTING.ordinal(), MENU_ID.SETTING.ordinal(), this.getString(R.string.menu_title_setting));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(MENU_ID.SETTING.getDrawableResId());

        // ★Twitter連携
        actionItem = menu.add(Menu.NONE, MENU_ID.TWITTER.ordinal(), MENU_ID.TWITTER.ordinal(), this.getString(R.string.menu_title_share));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(MENU_ID.TWITTER.getDrawableResId());

        // Facebook
        actionItem = menu.add(Menu.NONE, MENU_ID.FACEBOOK.ordinal(), MENU_ID.FACEBOOK.ordinal(), this.getString(R.string.menu_title_facebook));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(MENU_ID.FACEBOOK.getDrawableResId());

        // HISTORY
        actionItem = menu.add(Menu.NONE, MENU_ID.HISTORY.ordinal(), MENU_ID.HISTORY.ordinal(), this.getString(R.string.menu_title_history));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        actionItem.setIcon(MENU_ID.HISTORY.getDrawableResId());

        // LINE
        actionItem = menu.add(Menu.NONE, MENU_ID.LINE.ordinal(), MENU_ID.LINE.ordinal(), this.getString(R.string.menu_title_line));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem.setIcon(MENU_ID.LINE.getDrawableResId());

        // FEEDBACK
        actionItem = menu.add(Menu.NONE, MENU_ID.FEEDBACK.ordinal(), MENU_ID.FEEDBACK.ordinal(), this.getString(R.string.menu_title_feedback));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        actionItem.setIcon(MENU_ID.FEEDBACK.getDrawableResId());

        // LICENSE
        actionItem = menu.add(Menu.NONE, MENU_ID.LICENSE.ordinal(), MENU_ID.LICENSE.ordinal(), this.getString(R.string.menu_title_license));
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        actionItem.setIcon(MENU_ID.LICENSE.getDrawableResId());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View layout;
        MENU_ID menu_id = MENU_ID.getMenuIdEnum(item.getItemId());
        assert menu_id != null;
        switch (menu_id) {
            case SETTING:
                FlurryAgent.logEvent("Setting");
                // リスト表示用のアラートダイアログ
                layout = inflater.inflate(R.layout.setting, (ViewGroup) findViewById(R.id.layout_root));

                mAlertDialog = new AlertDialog.Builder(this);
                mAlertDialog.setTitle(this.getString(R.string.dialog_title_setting));
                mAlertDialog.setIcon(R.drawable.ic_settings_black_48dp);
                mAlertDialog.setView(layout);
                mAlertDialog.setNegativeButton(this.getString(R.string.dialog_button_ok), null);

                setSeekBarInLayout(layout);
                setSwitchInLayout(layout);
                setRadioGroupInLayout(layout);
                setToggleButtonInLayout(layout);
                mAlertDialog.show();
                break;
            case TWITTER:
                FlurryAgent.logEvent("Share twitter");
                if (!TwitterUtility.hasAccessToken(this)) {
                    startAuthorize();
                } else {
                    layout = inflater.inflate(R.layout.sharetotwitter, (ViewGroup) findViewById(R.id.layout_root_twitter));
                    String timeStamp = new SimpleDateFormat(DateUtil.DATE_FORMAT.YYYYMMDD_HHmmss.getFormat(), Locale.JAPAN).format(Calendar.getInstance().getTime());
                    mAlertDialog = new AlertDialog.Builder(this);
                    mAlertDialog.setTitle(this.getString(R.string.dialog_title_tweet));
                    mAlertDialog.setIcon(R.drawable.ic_share_black_48dp);
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
                    mAlertDialog.show();
                }
                break;
            case HISTORY:
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case FACEBOOK:
                showShareDialog();
                break;
            case LINE:
                LINEUtil.sendStringMessage(this, getString(R.string.twitter_tweetText) + "\n" +
                        String.format(getString(R.string.app_googlePlay_url), getPackageName()));
                break;
            case FEEDBACK:
                Toast.makeText(this, "ご意見お待ちしております！", Toast.LENGTH_LONG).show();
                new EasyFeedback.Builder(this)
                        .withEmail("0825elle@gmail.com")
                        .build()
                        .start();
                break;
            case LICENSE:
                new LibsBuilder()
                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
//                        .withAboutDescription("T")
                        //start the activity
                        .start(this);
                break;

        }
        return true;
    }

    private Date startDateTime;

    private HistoryItemModel createHistoryItemData() {
        HistoryItemModel historyItemModel = new HistoryItemModel();
        historyItemModel.setStartDateTime(startDateTime);
        historyItemModel.setEndDateTime(new Date());
        historyItemModel.setStepCount(String.valueOf(mStepCount) + getString(R.string.home_step_count_dimension));
        String stepCount = ((TextView) findViewById(R.id.txtStepCount)).getText().toString();
        historyItemModel.setStepCountAlert(stepCount);
        return historyItemModel;
    }

    public void setStartButtonFunction(final View v) {
        DefaultLayoutPromptView promptView = (DefaultLayoutPromptView) findViewById(R.id.prompt_view);
        promptView.setVisibility(View.GONE);

        if (mAlertService.IsRunningAlertService()) {
            FlurryAgent.logEvent("Service Stop!!");
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.END_DATE, DateUtil.getNowDate(DateUtil.DATE_FORMAT.YYYYMMDDhhmmss));
            mFirebaseAnalytics.logEvent("service_stop", bundle);

            mStepCount = mAlertService.getStepCountCurrent();
            changeViewState(false, ((ActionButton) v));
            Toast.makeText(this, getString(R.string.app_used_thankyou), Toast.LENGTH_SHORT).show();

            if (mStepCount > 0) {
                RealmUtil.insertHistoryItemAsync(realm, createHistoryItemData(), new RealmUtil.realmTransactionCallbackListener() {
                    @Override
                    public void OnSuccess() {

                    }

                    @Override
                    public void OnError() {

                    }
                });
            }

            if (TwitterUtility.hasAccessToken(getApplicationContext())) {
                Context context = MainActivity.this;
                LayoutInflater inflater = LayoutInflater.from(context);
                View layout = inflater.inflate(R.layout.sharetotwitter, (ViewGroup) findViewById(R.id.layout_root_twitter));
                mAlertDialog = new AlertDialog.Builder(context);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                mAlertDialog.setTitle(context.getString(R.string.dialog_tweet));
                mAlertDialog.setIcon(R.drawable.ic_share_black_48dp);
                mAlertDialog.setView(layout);
                mTweetText = (EditText) layout.findViewById(R.id.edtTweet);
                mTweetText.setText(String.valueOf(mStepCount) + getString(R.string.twitter_tweet_step) + "\n" + getString(R.string.twitter_tweetText) + "\n" +
                        String.format(getString(R.string.app_googlePlay_url), getPackageName()) + "\n" + timeStamp);
                mAlertDialog.setPositiveButton(context.getString(R.string.dialog_button_send), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tweet(mTweetText.getText().toString());
                    }
                });
                mAlertDialog.setCancelable(false);
                mAlertDialog.setNegativeButton(context.getString(R.string.dialog_button_cancel), null);
                mAlertDialog.show();
            }
            mAlertService.stopSensors();
//            mAlertService.setIsBoundService(false);
            displayInterstitial();

            mbtnStart.setShowAnimation(ActionButton.Animations.SCALE_UP);
            mbtnStart.playShowAnimation();

        } else {
            if (mStepCount > 0) {
                mAlertDialog = new AlertDialog.Builder(MainActivity.this);
                mAlertDialog.setIcon(R.drawable.ic_stat_small);
                mAlertDialog.setMessage("歩数を0に戻してよろしいですか？");
                mAlertDialog.setPositiveButton("0に戻す", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String initial = getString(R.string.zero) + getString(R.string.home_step_count_dimension);
                        ((TextView) findViewById(R.id.txtStepCount)).setText(initial);
                        ((TextView) findViewById(R.id.txtStepNo)).setText(initial);
                        start(v);
                    }
                });
                mAlertDialog.setNeutralButton("続きから", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldContinue = true;
                        start(v);
                    }
                });
                mAlertDialog.setNegativeButton("いいえ", null);
                mAlertDialog.show();
            } else {
                start(v);
            }
        }
    }

    private boolean shouldContinue = false;

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /** check if we already  have permission to draw over other apps */
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void start(View v) {
        if (checkOverlayPermission()) {
            mAlertService.startOverlay();
            startProcesses(v);
        } else {
            /** if not construct intent to request permission */
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
//            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
            /** request permission via start activity for result */
            startActivityForResult(i, CHECK_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    public static int CHECK_OVERLAY_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                mAlertService.startOverlay();
                startProcesses(mbtnStart);
            } else {
                startProcesses(mbtnStart);
            }
        }
    }

    private void startProcesses(View v) {
        FlurryAgent.logEvent("Service Start!!");
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.START_DATE, DateUtil.getNowDate(DateUtil.DATE_FORMAT.YYYYMMDDhhmmss));
        mFirebaseAnalytics.logEvent("service_stop", bundle);

        changeViewState(true, ((ActionButton) v));
        mAlertService.startSensors(shouldContinue);
        shouldContinue = false;

        startDateTime = new Date();
        mbtnStart.setShowAnimation(ActionButton.Animations.SCALE_UP);
        mbtnStart.playShowAnimation();
    }

    public void setSeekBarInLayout(View layout) {
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.skbSensitivity);
        seekBar.setProgress(mAlertStartAngle - ALERT_ANGLE_INITIAL_OFFSET);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        FlurryAgent.logEvent("Setting SeekBar");
                        mAlertStartAngle = progress + ALERT_ANGLE_INITIAL_OFFSET;
                        if (mAlertService != null && mAlertService.isBoundService()) {
                            mAlertService.setAlertStartAngle(mAlertStartAngle);
                        }
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
        final RadioButton radioButtonTop = (RadioButton) layout.findViewById(R.id.radiobutton_top);
        final RadioButton radioButtonCenter = (RadioButton) layout.findViewById(R.id.radiobutton_center);
        final RadioButton radioButtonBottom = (RadioButton) layout.findViewById(R.id.radiobutton_bottom);
        Switch swh = (Switch) layout.findViewById(R.id.swhToastOnOff);
        swh.setChecked(mIsToastOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mIsToastOn = isChecked;
                        if (mAlertService != null && mAlertService.isBoundService()) {
                            mAlertService.setIsToastOn(mIsToastOn);
                        }
                        radioButtonBottom.setEnabled(isChecked);
                        radioButtonTop.setEnabled(isChecked);
                        radioButtonCenter.setEnabled(isChecked);
                    }
                }
        );
        swh = (Switch) layout.findViewById(R.id.swhVibrationOnOff);
        swh.setChecked(mIsVibrationOn);
        swh.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mIsVibrationOn = isChecked;
                        if (mAlertService != null && mAlertService.isBoundService()) {
                            mAlertService.setIsVibrationOn(mIsVibrationOn);
                        }
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

        if (mAlertService != null) {
            swh.setEnabled(!mAlertService.IsRunningAlertService());
        } else {
            swh.setEnabled(true);
        }
    }

    public void setRadioGroupInLayout(View layout) {
        RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radiogroup);
        RadioButton radioButtonTop = (RadioButton) layout.findViewById(R.id.radiobutton_top);
        RadioButton radioButtonCenter = (RadioButton) layout.findViewById(R.id.radiobutton_center);
        RadioButton radioButtonButton = (RadioButton) layout.findViewById(R.id.radiobutton_bottom);
        switch (mToastPosition) {
            case Gravity.TOP:
                radioButtonTop.setChecked(true);
                break;
            case Gravity.CENTER:
                radioButtonCenter.setChecked(true);
                break;
            case Gravity.BOTTOM:
                radioButtonButton.setChecked(true);
                break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radiobutton_top:
                        mToastPosition = Gravity.TOP;
                        break;
                    case R.id.radiobutton_center:
                        mToastPosition = Gravity.CENTER;
                        if (mAlertService != null) {
                            mAlertService.setToastPosition(Gravity.CENTER);
                        }
                        break;
                    case R.id.radiobutton_bottom:
                        mToastPosition = Gravity.BOTTOM;
                        break;
                }
                if (mAlertService != null && mAlertService.isBoundService()) {
                    mAlertService.setToastPosition(mToastPosition);
                }
            }
        });
    }

    public void setToggleButtonInLayout(View layout) {
        ToggleButton tgbPedometer = (ToggleButton) layout.findViewById(R.id.tgbPedometer);
        tgbPedometer.setChecked(mShouldShowPedometer);
        tgbPedometer.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mShouldShowPedometer = isChecked;
                        if (isChecked) {
                            findViewById(R.id.llStepCount).setVisibility(View.VISIBLE);
                            findViewById(R.id.txtExplanation).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llStepCount).setVisibility(View.INVISIBLE);
                            if (mAlertService.IsRunningAlertService()) {
                                findViewById(R.id.txtExplanation).setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
        );
    }

    // イベントリスナーの登録を解除
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil.saveBoolean(this, SETTING_SHAREDPREF_NAME, "message", mIsToastOn);
        SharedPreferencesUtil.saveBoolean(this, SETTING_SHAREDPREF_NAME, "vibrate", mIsVibrationOn);
        SharedPreferencesUtil.saveBoolean(this, SETTING_SHAREDPREF_NAME, "passcode", mPasscodeOn);
        SharedPreferencesUtil.saveInt(this, SETTING_SHAREDPREF_NAME, "progress", mAlertStartAngle - ALERT_ANGLE_INITIAL_OFFSET);
        SharedPreferencesUtil.saveBoolean(this, SETTING_SHAREDPREF_NAME, "pedometer", mShouldShowPedometer);
        SharedPreferencesUtil.saveInt(this, SETTING_SHAREDPREF_NAME, "toastPosition", mToastPosition);
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // FIXME : これどこで呼ぶのが一番いいのか
        Growthbeat.getInstance().stop();
    }


    /**
     * bindServiceでは bind だけでserviceはstartされる
     * serviceは存在していてActivity再起動後、bindServiceすると
     * →こうするとRebindしてonServiceConnectedが呼ばれる
     */
    public void killAlertService() {
        unbindService(mConnection);
        mAlertService.setIsBoundService(false);
        mConnection = null;
        mAlertService.removeOnActionFromNotificationListener();
        mAlertService.removeOnWalkStepListener();
        mAlertService.stopSensors();
        mAlertService = null;

//        stopService(mAlertServiceIntent);
    }

    @Override
    public void onBackPressed() {
        // これをコメントアウトしないとバックキー押してすぐアプリがBackgroundに回ってしまう
//        super.onBackPressed();
//        // 端末のホーム画面に戻る
//        moveTaskToBack(true);
        mAlertDialog = new AlertDialog.Builder(MainActivity.this);
        mAlertDialog.setMessage(getString(R.string.dialog_back_key_message));
        mAlertDialog.setPositiveButton(getString(R.string.dialog_back_key_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                killAlertService();
                finish();
            }
        });
        mAlertDialog.setNegativeButton(getString(R.string.dialog_back_key_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        mAlertDialog.show();
    }

    public void changeViewState(boolean isStart, ActionButton button) {
        if (isStart) {
            button.setButtonColor(ContextCompat.getColor(this, R.color.colorAccent));
            button.setButtonColorPressed(ContextCompat.getColor(this, R.color.colorAccentDark));
            ((TextView) findViewById(R.id.txtStartButton)).setText(getString(R.string.stop));
            findViewById(R.id.txtWatching).setVisibility(View.VISIBLE);
            findViewById(R.id.txtWatching).startAnimation(mAnimationBlink);
            if (!mShouldShowPedometer) {
                findViewById(R.id.txtExplanation).setVisibility(View.INVISIBLE);
            }
            findViewById(R.id.txtExplanation).setVisibility(View.GONE);//.setText("歩きスマホ中の歩数を計測中");
        } else {
            button.setButtonColor(ContextCompat.getColor(this, R.color.colorPrimary));
            button.setButtonColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            ((TextView) findViewById(R.id.txtStartButton)).setText(getString(R.string.start));
            findViewById(R.id.txtWatching).setVisibility(View.GONE);
            findViewById(R.id.txtExplanation).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtExplanation)).setText(getString(R.string.app_explanation));

//            setShouldShowAlert(false);
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
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.twitter_auth_succeed), Toast.LENGTH_LONG).show();
                    TwitterUtility.storeAccessToken(getApplicationContext(), accessToken);
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.twitter_auth_fail), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.twitter_tweet_succeed), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.twitter_tweet_fail), Toast.LENGTH_LONG).show();
                }
            }
        };
        task.execute(tweetString);
    }

    // TODO : ToastUtilを利用しよう
    private Toast createToastShort(String text) {
        Toast toast = new Toast(this);
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TOAST_TEXT_SIZE);
//        int pixel = (int)mWindowDensity * 56;
        int toastMargin = getResources().getDimensionPixelSize(R.dimen.toast_margin_top_bottom);
        tv.setPadding(0, toastMargin, 0, toastMargin);
        toast.setView(tv);
        toast.setGravity(mToastPosition, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);

        return toast;
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void startBtnClick(final View v) {
        if (mPasscodeOn) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            final View layout = inflater.inflate(R.layout.passcode, (ViewGroup) findViewById(R.id.layout_root_passcode));
            final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog(MainActivity.this)
                    .setTitle(getString(R.string.dialog_passcode_title))
                    .setDescription(getString(R.string.dialog_passcode_description))
                    .setIcon(R.drawable.ic_lock_blue_grey_100_48dp)
                    .setCustomView(layout).show();

            final PasscodeView passcodeView = (PasscodeView) layout.findViewById(R.id.passcode);
            final PasscodeView passcodeViewConfirm = (PasscodeView) layout.findViewById(R.id.passcodeConfirm);
            final TextView txtPasscodeConfirm = (TextView) layout.findViewById(R.id.txtPasscodeConfirm);
            txtPasscodeConfirm.setText(getString(R.string.dialog_passcode_confirm));
            ((TextView) layout.findViewById(R.id.txtPasscode)).setText(getString(R.string.dialog_passcode_passcode));
            passcodeView.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
                @Override
                public void onPasscodeEntered(String passcode) {
                    if (mAlertService != null && mAlertService.IsRunningAlertService()) {
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
}
