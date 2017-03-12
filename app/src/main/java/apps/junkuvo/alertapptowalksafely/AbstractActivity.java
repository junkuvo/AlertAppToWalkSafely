package apps.junkuvo.alertapptowalksafely;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public abstract class AbstractActivity extends AppCompatActivity {

    protected Realm realm;
    @VisibleForTesting
    static final String IS_RUNNING_JUNIT = "IS_RUNNING_JUNIT";
    @VisibleForTesting
    boolean isRunningJunit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isRunningJunit = getIntent().getBooleanExtra(IS_RUNNING_JUNIT, false);

        if (!isRunningJunit) {
            // realmの初期化
            Realm.init(this);
            RealmConfiguration config = new RealmConfiguration.Builder().build();
            Realm.setDefaultConfiguration(config);
            realm = Realm.getDefaultInstance();
        }


        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    protected CallbackManager callbackManager;
    protected ShareDialog shareDialog;
    protected ShareLinkContent shareLinkContent;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isRunningJunit) {
            realm.close();
        }
    }

    protected void showShareDialog() {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle(getString(R.string.twitter_tweetText))
                    .setContentDescription(getString(R.string.intro_description))
                    .setContentUrl(Uri.parse(String.format(getString(R.string.app_googlePlay_url), getPackageName())))
                    .build();

            shareDialog.show(shareLinkContent);
        }
    }
}
