package apps.junkuvo.alertapptowalksafely.utils;

import android.text.format.DateFormat;

import java.util.Date;

public class DateUtil {

    public enum DATE_FORMAT {
        YYYYMMDD("yyyy/MM/dd"),
        YYYYMMDDhhmmss("yyyy/MM/dd hh:mm:ss"),
        hhmmss("hh:mm:ss");

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
}
