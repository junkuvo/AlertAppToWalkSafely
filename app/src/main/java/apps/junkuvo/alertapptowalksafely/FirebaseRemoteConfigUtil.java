package apps.junkuvo.alertapptowalksafely;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class FirebaseRemoteConfigUtil {

    public static void initialize() {

        //instance取得
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        //デベロッパーモード指定
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);

        //デフォルトの値を読み込む
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public static void fetch(long cacheExpirationSeconds) {
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetch(cacheExpirationSeconds)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //値を反映
                            firebaseRemoteConfig.activateFetched();
                        } else {
                            //fetch失敗
                        }
                    }
                });
    }

}
