package apps.junkuvo.alertapptowalksafely;

import android.content.res.Configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeviceAttitudeCalculatorTest {

    DeviceAttitudeCalculator deviceAttitudeCalculator;

    @Before
    public void setUp() throws Exception {
        deviceAttitudeCalculator = new DeviceAttitudeCalculator(true, Configuration.ORIENTATION_PORTRAIT);


    }

    @After
    public void tearDown() throws Exception {
//        deviceAttitudeCalculator.calculateDeviceAttitude(new SensorEvent())

    }

    @Test
    public void calculateDeviceAttitude() throws Exception {

    }

}