package apps.junkuvo.alertapptowalksafely;

import android.app.Application;

import com.github.stkent.amplify.tracking.Amplify;


public class AlertApplication extends Application{
    private static final String TAG = AlertApplication.class.getSimpleName();
    private final AlertApplication self = this;

    @Override
    public void onCreate() {
        super.onCreate();

        Amplify.get(this)
                .setFeedbackEmailAddress("0825elle@gmail.com")
                .setAlwaysShow(true);
//        Amplify.get(this)
//                .setFeedbackEmailAddress("0825elle@gmail.com")
//                .applyAllDefaultRules();


    }
}
