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
import com.optimizely.Optimizely;


public class AlertApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(false)
                .build(this, getString(R.string.flurry_key));

        Growthbeat.getInstance().initialize(this, getString(R.string.growthpush_application_id), getString(R.string.growthpush_credential_id));
        GrowthPush.getInstance().requestRegistrationId(getString(R.string.gcm_sender_id), BuildConfig.DEBUG ? Environment.development : Environment.production);
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
                    Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.app_googlePlay_url), getPackageName())));
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

        // You can find the following code snippet in your project code.
        Optimizely.startOptimizelyWithAPIToken("<API Token>", this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
