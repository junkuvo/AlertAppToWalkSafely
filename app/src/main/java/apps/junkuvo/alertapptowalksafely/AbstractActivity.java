package apps.junkuvo.alertapptowalksafely;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AbstractActivity extends AppCompatActivity {

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isRunningJunit) {
            realm.close();
        }
    }
}
