package apps.junkuvo.alertapptowalksafely;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.flurry.android.FlurryAgent;
import com.github.stkent.amplify.tracking.Amplify;
import com.github.stkent.amplify.tracking.PromptInteractionEvent;
import com.github.stkent.amplify.tracking.rules.MaximumCountRule;
import com.growthbeat.Growthbeat;
import com.growthpush.GrowthPush;
import com.growthpush.handler.DefaultReceiveHandler;
import com.growthpush.model.Environment;


public class AlertApplication extends MultiDexApplication {
    private static final String TAG = AlertApplication.class.getSimpleName();
    private final AlertApplication self = this;

    // サービスが動いているかどうかのフラグ
    private boolean sIsRunningService = false;

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(false)
                .build(this, "FNY7NQCRJW697JJQH353");

        Growthbeat.getInstance().initialize(this, "Pkjf4L4sbDeKDs6I", "zYdJEQjTt5xnP78iQXUSO4zjs7eTX8CN");
        GrowthPush.getInstance().requestRegistrationId("1047305644128", BuildConfig.DEBUG ? Environment.development : Environment.production);
        Growthbeat.getInstance().start();
        // Launchイベントの取得
        GrowthPush.getInstance().trackEvent("Launch");
        // DeviceTagの取得
        GrowthPush.getInstance().setDeviceTags();

        GrowthPush.getInstance().setReceiveHandler(new DefaultReceiveHandler(new DefaultReceiveHandler.Callback() {
            @Override
            public void onOpen(Context context, Intent intent) {
//                String url = intent.getExtras().getString("url");
//                if (url == null) {
//                    super.onOpen(context, intent);
//                    return;
//                }
                Uri uri = Uri.parse(String.format(getApplicationContext().getString(R.string.review_url), getApplicationContext().getPackageName()));
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    context.startActivity(i);
                } catch (ActivityNotFoundException activityNotFound) {
                    // to handle play store not installed scenario
                    Intent i2 = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                    context.startActivity(i2);
                }

            }
        }));

//        Amplify.initSharedInstance(this)
//                .setFeedbackEmailAddress("someone@example.com")
//                .setAlwaysShow(true);
//        Amplify.get(this)
//                .setFeedbackEmailAddress("0825elle@gmail.com")
//                .applyAllDefaultRules();

        Amplify.initSharedInstance(this)
                .setFeedbackEmailAddress("0825elle@gmail.com")
                .setInstallTimeCooldownDays(3) // Prompt not shown input days of initial install.
                .setLastUpdateTimeCooldownDays(3) // Prompt not shown within input days of most recent update.
                .setLastCrashTimeCooldownDays(3) // Prompt not shown within input days of most recent crash.
                .addTotalEventCountRule(PromptInteractionEvent.USER_GAVE_POSITIVE_FEEDBACK,
                        new MaximumCountRule(1)); // Never ask the user for feedback again if they already responded positively.
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean IsRunningService() {
        return sIsRunningService;
    }

    public void setIsRunningService(boolean sIsRunningService) {
        this.sIsRunningService = sIsRunningService;
    }
}
