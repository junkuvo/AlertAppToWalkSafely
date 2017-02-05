package apps.junkuvo.alertapptowalksafely;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;

public class AbstractActivity extends AppCompatActivity {

    protected Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
