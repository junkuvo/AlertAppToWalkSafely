package apps.junkuvo.alertapptowalksafely;

import android.app.Application;

import com.github.stkent.amplify.tracking.Amplify;
import com.github.stkent.amplify.tracking.PromptViewEvent;
import com.github.stkent.amplify.tracking.rules.MaximumCountRule;


public class AlertApplication extends Application{
    private static final String TAG = AlertApplication.class.getSimpleName();
    private final AlertApplication self = this;

    @Override
    public void onCreate() {
        super.onCreate();

//        Amplify.get(this)
//                .setFeedbackEmailAddress("0825elle@gmail.com")
//                .setAlwaysShow(true);
//        Amplify.get(this)
//                .setFeedbackEmailAddress("0825elle@gmail.com")
//                .applyAllDefaultRules();

        Amplify.get(this)
                .setFeedbackEmailAddress("0825elle@gmail.com")
                .setInstallTimeCooldownDays(3) // Prompt not shown input days of initial install.
                .setLastUpdateTimeCooldownDays(3) // Prompt not shown within input days of most recent update.
                .setLastCrashTimeCooldownDays(3) // Prompt not shown within input days of most recent crash.
                .addTotalEventCountRule(PromptViewEvent.USER_GAVE_POSITIVE_FEEDBACK,
                        new MaximumCountRule(1)); // Never ask the user for feedback again if they already responded positively.
    }
}
