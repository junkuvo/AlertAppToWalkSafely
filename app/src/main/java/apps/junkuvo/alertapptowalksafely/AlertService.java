package apps.junkuvo.alertapptowalksafely;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.stkent.amplify.utils.StringUtils;

import java.util.Date;
import java.util.List;

import apps.junkuvo.alertapptowalksafely.models.WalkServiceData;
import apps.junkuvo.alertapptowalksafely.utils.RealmUtil;
import junkuvo.apps.androidutility.ToastUtil;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static apps.junkuvo.alertapptowalksafely.MainActivity.EXTRA_KEY_CAN_SHOW_OVERLAY_FLAG;
import static apps.junkuvo.alertapptowalksafely.MainActivity.EXTRA_KEY_NEW_FUNCTION;
import static apps.junkuvo.alertapptowalksafely.MainActivity.EXTRA_KEY_SHOULD_CONTINUE_COUNT_FLAG;
import static apps.junkuvo.alertapptowalksafely.MainActivity.EXTRA_KEY_START_DATE;
import static apps.junkuvo.alertapptowalksafely.models.WalkServiceData.CLICK_NOTIFICATION;
import static apps.junkuvo.alertapptowalksafely.models.WalkServiceData.DELETE_NOTIFICATION;

public class AlertService extends Service implements SensorEventListener {

    public static final String ACTION = "AlertService";
    private static final float TOAST_TEXT_SIZE = 24; // sp

    private SensorManager mSensorManager;

    // 歩きスマホの判定に利用（姿勢の計算）
    private DeviceAttitudeCalculator mDeviceAttitudeCalculator;

    // 歩数カウントに利用（ステップセンサがない場合）
    private WalkCountCalculator mWalkCountCalculator;
    private WalkCountCalculator mWalkCountCalculatorAsNg;

    private int mTendencyCheckCount = 0;
    private int mTendencyOutCount = 0;

    public Context mContext;
    private boolean mHasStepFeature = false;

    private WalkServiceAdapter walkServiceAdapter;

    // 画面のON/OFFを検知する
    private boolean mIsScreenOn = true;

    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int stepCount = intent.getIntExtra("stepCount", mStepCountCurrent);
            if (intent.getBooleanExtra("isStepCounter", false)) {
//                mStepCountCurrent = stepCount < 0 ? 0 : stepCount;
//                onWalkStepListener.onAlertWalkStep(mStepCountCurrent);
            } else {
                // 歩きスマホの注意
                if (WalkServiceData.getInstance().IsToastOn()) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showCustomToastWithImage(getApplicationContext(),
                                    WalkServiceData.getInstance().getAlertMessage(),
                                    R.color.fab_material_white, TOAST_TEXT_SIZE,
                                    WalkServiceData.getInstance().getToastPosition());
                        }
                    });
                }

                if (WalkServiceData.getInstance().IsVibrationOn()) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {100, 100, 100, 100, 100, 100};
                    vibrator.vibrate(pattern, -1);
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receive screen off
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mIsScreenOn = false;
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mIsScreenOn = true;
            }
            if (intent.getAction().equals(CLICK_NOTIFICATION)) {
                Intent startActivityIntent = new Intent(mContext, MainActivity.class);
                startActivityIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(startActivityIntent);
                return;
            }

            if (intent.getAction().equals(DELETE_NOTIFICATION)) {
                stopSensors();
                walkServiceAdapter.notifyActionFromNotification(DELETE_NOTIFICATION);
            }
        }
    };

    private IntentFilter intentFilter = new IntentFilter();
    private IntentFilter localIntentFilter = new IntentFilter();

    private Configuration config;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        walkServiceAdapter = ((AlertApplication) getApplication()).getWalkServiceAdapter();
        boolean isTablet = Utility.isTabletNotPhone(mContext);
        Resources resources = mContext.getResources();
        config = resources.getConfiguration();
        mDeviceAttitudeCalculator = new DeviceAttitudeCalculator(isTablet, config.orientation);
        mWalkCountCalculator = new WalkCountCalculator();
        mWalkCountCalculatorAsNg = new WalkCountCalculator();

        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(CLICK_NOTIFICATION);
        intentFilter.addAction(DELETE_NOTIFICATION);
        localIntentFilter.addAction(ACTION);

        mHasStepFeature = isHasStepFeature();

    }

    private View overlay;
    private WindowManager windowManager;
    private boolean enableNewFunction = false;

    /**
     * これはonStartServiceでしか呼ばれない
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        boolean shouldContinue = intent.getBooleanExtra(EXTRA_KEY_SHOULD_CONTINUE_COUNT_FLAG, true);
        startSensors(shouldContinue);
        boolean canShowOverlay = intent.getBooleanExtra(EXTRA_KEY_CAN_SHOW_OVERLAY_FLAG, false);
        if (canShowOverlay) {
            startOverlay();
        }

        enableNewFunction = intent.getBooleanExtra(EXTRA_KEY_NEW_FUNCTION, false);

        Date startDate;
        try {
            startDate = (Date) intent.getSerializableExtra(EXTRA_KEY_START_DATE);
        } catch (Exception e) {
            startDate = new Date();
        }
        WalkServiceData.getInstance().setStartDate(startDate);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSensors();
    }

    private PendingIntent getPendingIntentWithBroadcast(String action) {
        return PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(action), FLAG_UPDATE_CURRENT);
    }

    // BindしたServiceをActivityに返す
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
    }

    // センサーの精度が変更されると呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * センサーをスタートさせる。これで監視が始まる。
     *
     * @param shouldContinue 歩数を継続して利用するかどうかのフラグ true 継続利用
     */
    public void startSensors(boolean shouldContinue) {
        startServiceForeground();
        try {
            WalkServiceData.getInstance().setRunningService(true);

            LocalBroadcastManager.getInstance(mContext).registerReceiver(localBroadcastReceiver, localIntentFilter);
            registerReceiver(broadcastReceiver, intentFilter);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            // センサーのオブジェクトリストを取得する
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) {
                Sensor s = sensors.get(0);
                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }

            // 歩数計用のセンサー登録
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Sensor mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            initializeSensingValues(shouldContinue);
        } catch (Exception e) {
            stopSensors();
            e.printStackTrace();
        }
    }

    public void stopSensors() {
        try {
            if (overlay != null) {
                windowManager.removeView(overlay);
            }
            // Notificationを消す
            stopForeground(true);

            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
                mSensorManager = null;
            }
            if (localBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(localBroadcastReceiver);
            }

            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }

            // 0以上で、広告タップ済みかすでに履歴機能を利用している場合にはInsert実行
            if (StringUtils.isNotBlank(WalkServiceData.getInstance().getWalkCountAll())
                    && !WalkServiceData.getInstance().getWalkCountAll().equals("0")
                    && enableNewFunction) {
                RealmUtil.insertHistoryItemAsync(((AlertApplication) getApplication()).getRealm()
                        , RealmUtil.createHistoryItemData(this, WalkServiceData.getInstance().getWalkCountAll(), WalkServiceData.getInstance().getWalkCountAlert())
                        , new RealmUtil.realmTransactionCallbackListener() {
                            @Override
                            public void OnSuccess() {

                            }

                            @Override
                            public void OnError() {

                            }
                        });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            WalkServiceData.getInstance().setRunningService(false);
        }
    }

    private WindowManager.LayoutParams layoutParams;

    public void startOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.y = getResources().getDimensionPixelSize(R.dimen.overlay_y_offset_106dp);// 適当な値

        final Point point = getDisplaySize();

        overlay = LayoutInflater.from(this).inflate(R.layout.overlay, null);
        overlay.findViewById(R.id.fabStartOverlay).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (event.getAction()) {
                    case ACTION_DOWN:
                        initialX = x;
                        initialY = y;
                    case ACTION_MOVE:
                        windowManager.getDefaultDisplay().getRealSize(point);
                        int centerX;
                        int centerY;
                        centerX = x - (point.x / 2) - v.getContext().getResources().getDimensionPixelSize(R.dimen.basic_margin_8dp);// FIXME :ここも縦横で変更
                        centerY = y - (point.y / 2) + v.getContext().getResources().getDimensionPixelSize(R.dimen.basic_margin_22dp);
                        layoutParams.x = centerX;
                        layoutParams.y = centerY;
                        windowManager.updateViewLayout(overlay, layoutParams);
                        break;
                    case ACTION_UP:
                        isMoved = Math.sqrt(Math.pow(x - initialX, 2) + Math.pow(y - initialY, 2)) > v.getContext().getResources().getDimensionPixelSize(R.dimen.basic_margin_8dp);
                        break;
                }
                return false;
            }
        });
        overlay.findViewById(R.id.fabStartOverlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!isMoved) {
                    onShowPopup(v);
                }
                isMoved = false;
            }
        });

        windowManager.addView(overlay, layoutParams);
    }

    private int initialX = 0;
    private int initialY = 0;

    private void onShowPopup(final View v) {
        Context wrapper = new ContextThemeWrapper(v.getContext(), R.style.MyPopupMenu);
        // PopupMenuのインスタンスを作成
        PopupMenu popup = new PopupMenu(wrapper, v);

        // popup.xmlで設定したメニュー項目をポップアップメニューに割り当てる
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());

        // ポップアップメニューを表示
        popup.show();

        // ポップアップメニューのメニュー項目のクリック処理
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // 押されたメニュー項目名をToastで表示
                switch (item.getItemId()) {
                    case R.id.hide:
                        if (overlay != null) {
                            windowManager.removeView(overlay);
                        }
                        break;
//                                case R.id.show_alert_step_count:
//                                    break;
                    case R.id.open_app:
//                        walkServiceAdapter.getOverlayActionListener().onOpenApp();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    private Point getDisplaySize() {
        Point point = new Point(0, 0);
        Display display = windowManager.getDefaultDisplay();
        // Android 4.2~
        display.getRealSize(point);
        return point;
    }

    private boolean isMoved = false;

    public void initializeSensingValues(boolean shouldContinue) {
        mTendencyCheckCount = 0;
        mTendencyOutCount = 0;
        if (shouldContinue) {
            mStepCountCurrent = WalkServiceData.getInstance().getWalkCountAllInt() - 1;
            countAsNg = WalkServiceData.getInstance().getWalkCountAlertInt();
        } else {
            mStepCountCurrent = -1;
            countAsNg = 0;
        }
        showStepCount();
    }

    // FIXME ここの計算ロジックはワーカースレッドで実行すべき
    // センサーの値が変化すると呼ばれる(加速度・ステップディテクター・ステップカウンター)
    @Override
    public void onSensorChanged(final SensorEvent event) {
        // 画面がONの場合、歩きスマホを検知する
        final Sensor sensor = event.sensor;
        if (walkServiceAdapter.isWalkServiceRunning()) {
            if (mIsScreenOn) {
                if (mStepCountBefore == 0) {
                    mStepCountBefore = mStepCountCurrent;
                }

                if (isEveryOneSecond()) {
                    // 歩行中であることを判定
                    if (isWalking()) {

                        // 歩いている状態で下記にてデバイス角度計算
                        int tendency = mDeviceAttitudeCalculator.calculateDeviceAttitude(event);
                        int startAngle = WalkServiceData.getInstance().getAlertStartAngle();
                        // 下向きかどうかの判定
                        // 激しく動かすなどするとマイナスの値が出力されることがあるので tendency > 0 とする
                        // さらにテーブルに置いたときなど、水平状態があり得るため tendency > (適当) とする
                        if ((tendency > 180 - startAngle || tendency < startAngle) && tendency > 8) {
                            mTendencyOutCount++;
                            shouldCountAsNg = true;
                            //  下向きと判定されるのが連続 n 回(= n 秒)の場合、Alertを表示させる
                            if (mTendencyOutCount == 5) {
                                // 歩数計センサの利用：
                                Intent intent = new Intent(ACTION);
                                intent.putExtra("isStepCounter", false);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);

                                mTendencyOutCount = 0;
                                mStepCountBefore = mStepCountCurrent;
                            }
                        } else {
                            if (mTendencyOutCount > 0) {
                                mTendencyOutCount--;
                            }
                            shouldCountAsNg = false;
                        }
                    }
                    mTendencyCheckCount = 0;
                }
            } else {
                // FIXME : 本来はScreenがOffの場合、センサを停止したい
                shouldCountAsNg = false;
                mTendencyCheckCount = 0;
                mTendencyOutCount = 0;
            }

            // 歩数計の値を取得
            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                mStepCountCurrent++;
                if (shouldCountAsNg) {
                    countAsNg++;
                    // 歩きスマホ数のコールバック
                    walkServiceAdapter.notifyWalkDataAlertChanged(countAsNg);
                    // FIXME 色変えたいな
//                    ((ActionButton) overlay.findViewById(R.id.fabStartOverlay)).setButtonColor(R.color.colorAccent);
//                    ((ActionButton) overlay.findViewById(R.id.fabStartOverlay)).setButtonColorPressed(R.color.colorAccentDark);
//                    overlay.findViewById(R.id.fabStartOverlay).setAlpha(1.0F);
//                } else {
//                    ((ActionButton) overlay.findViewById(R.id.fabStartOverlay)).setButtonColor(R.color.colorPrimary);
//                    ((ActionButton) overlay.findViewById(R.id.fabStartOverlay)).setButtonColorPressed(R.color.colorPrimaryDark);
                }
                // 歩数を渡す
                showStepCount();
            } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (mIsScreenOn) {
                    // センサーモードSENSOR_DELAY_NORMALは200msごとに呼ばれるので
                    // 5回カウントして1秒ごとに下記を実行する(1秒くらいあれば歩数が変化している前提)
                    mTendencyCheckCount++;
                }
                // 歩行センサがない場合 3軸加速度から計算
                if (!mHasStepFeature) {
                    mStepCountCurrent = mWalkCountCalculator.walkCountCalculate(event);
                    if (shouldCountAsNg) {
                        countAsNg = mWalkCountCalculatorAsNg.walkCountCalculate(event);
                        // 歩きスマホ数のコールバック
                        walkServiceAdapter.notifyWalkDataAlertChanged(countAsNg < 0 ? 0 : countAsNg);
                    }
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("isStepCounter", true);
                    intent.putExtra("stepCount", mStepCountCurrent);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                    // 歩数を渡す
                    showStepCount();
                }
            }
        }
    }

    /**
     * 通常の歩行
     */
    private void showStepCount() {
        // 歩数を渡す
        if (overlay != null) {
            ((TextView) overlay.findViewById(R.id.overlay_text)).setText(String.valueOf(mStepCountCurrent));
        }

        walkServiceAdapter.notifyWalkDataNormalChanged(mStepCountCurrent < 0 ? 0 : mStepCountCurrent );
    }

    private int mStepCountBefore = 0;
    // onSensorChanged の TYPE_STEP_DETECT が最初に1回よばれるのでその分-1
    private int mStepCountCurrent = -1;
    // 歩きスマホ中として歩数をカウントするかどうかのフラグ
    private boolean shouldCountAsNg = false;
    // 歩きスマホ中の歩数。画面に表示される
    private int countAsNg = -1;

    // 歩数の変化を計算して、変化があれば歩行中と判定
    private boolean isWalking() {
        int mStepCountAfter = mStepCountCurrent;
        if (mStepCountBefore == mStepCountAfter) {
            return false;
        } else {
            mStepCountBefore = mStepCountAfter;
            return true;
        }
    }

    private boolean isEveryOneSecond() {
        return mTendencyCheckCount == 5;
    }

    public void startServiceForeground() {
        // サービスを永続化するために、通知を作成する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(getString(R.string.notification_ticker));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification_content_text));
//        builder.setSubText("アプリを開く際はタップしてください");
        builder.setSmallIcon(R.drawable.ic_stat_small);
        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        // FIXME : service から unbindする方法がないので、Notification から停止させる機能は一旦なくす
        builder.addAction(R.drawable.ic_stat_small, getString(R.string.home_button_stop), getPendingIntentWithBroadcast(DELETE_NOTIFICATION));

        builder.setContentIntent( //通知タップ時のPendingIntent
                getPendingIntentWithBroadcast(CLICK_NOTIFICATION)
        );
//        builder.setDeleteIntent(  //通知の削除時のPendingIntent
//                getPendingIntentWithBroadcast(DELETE_NOTIFICATION)
//        );

        NotificationCompat.BigTextStyle notificationBigTextStyle = new NotificationCompat.BigTextStyle(builder);
        builder.setStyle(notificationBigTextStyle);

        // ロックスクリーン上でどう見えるか
        builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);

        // PRIORITY_MINだとどこにも表示されなくなる
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        // サービス永続化
        startForeground(R.string.app_name, builder.build());
    }

    public boolean isHasStepFeature() {
        PackageManager packageManager = this.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

}

