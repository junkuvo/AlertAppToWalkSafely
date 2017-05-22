package apps.junkuvo.alertapptowalksafely.utils;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public enum DATE_FORMAT {
        YYYYMMDD("yyyy/MM/dd"),
        YYYYMMDDhhmmss("yyyy/MM/dd hh:mm:ss"),
        YYYYMMDD_HHmmss("yyyyMMdd_HHmmss"),
        HHmmss("HH:mm:ss");

        private String format;

        DATE_FORMAT(String s) {
            format = s;
        }

        public String getFormat() {
            return format;
        }
    }

    public static String convertDateToString(Date date, DATE_FORMAT date_format) {
        return DateFormat.format(date_format.getFormat(), date).toString();
    }

    public static String getNowDate(DATE_FORMAT date_format) {
        final SimpleDateFormat df = new SimpleDateFormat(date_format.format);
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
