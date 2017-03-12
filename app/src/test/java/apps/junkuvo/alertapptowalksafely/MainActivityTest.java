package apps.junkuvo.alertapptowalksafely;

import android.content.Intent;
import android.view.Gravity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ActivityController;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    MainActivity activity;
    ActivityController activityController;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * 設定値がPreferenceに保存され、onCreateで正しく読み込まれることを確認
     *
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
//        MainActivity activity = Robolectric.buildActivity(MainActivity.class).create().get();

//        ActivityController<EditActivity> controller = Robolectric.buildActivity(EditActivity.class, intent);
//        EditActivity activity = controller.setup().get();

        Intent intent = new Intent();
        // onCreateから実行
        intent.putExtra(AbstractActivity.IS_RUNNING_JUNIT, true);
        activityController = Robolectric.buildActivity(MainActivity.class, intent).create().start();
        activity = (MainActivity) activityController.get();

        boolean isToastOn = !activity.mIsToastOn;
        boolean isVibrationOn = !activity.mIsVibrationOn;
        boolean isShouldShowPedometer = !activity.mShouldShowPedometer;
        int toastPosition = activity.mToastPosition == Gravity.TOP ? Gravity.BOTTOM : Gravity.TOP;
        int alertStartAngle = activity.mAlertStartAngle - 10;
        activity.mIsToastOn = isToastOn;
        activity.mIsVibrationOn = isVibrationOn;
        activity.mShouldShowPedometer = isShouldShowPedometer;
        activity.mToastPosition = toastPosition;
        activity.mAlertStartAngle = alertStartAngle;

        // onDestroyまで実行
        activityController.pause().destroy();

        // onCreateから作り直す
        intent.putExtra(AbstractActivity.IS_RUNNING_JUNIT, true);
        activityController = Robolectric.buildActivity(MainActivity.class, intent).create().start();
        activity = (MainActivity) activityController.get();

        assertThat(isToastOn, is(activity.mIsToastOn));
        assertThat(isVibrationOn, is(activity.mIsVibrationOn));
        assertThat(isShouldShowPedometer, is(activity.mShouldShowPedometer));
        assertThat(toastPosition, is(activity.mToastPosition));
        assertThat(alertStartAngle, is(activity.mAlertStartAngle));
    }
}