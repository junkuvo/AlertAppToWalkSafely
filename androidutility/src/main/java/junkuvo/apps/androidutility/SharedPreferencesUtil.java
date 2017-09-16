package junkuvo.apps.androidutility;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesUtil {
    public enum PrefKeys{
        NOTIFICATION_SHOW_IN_BAR("NOTIFICATION_SHOW_IN_BAR");

        private String key;
        PrefKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static void saveString(Context context, String prefName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String prefName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void saveBoolean(Context context, String prefName, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String prefName, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void saveInt(Context context, String prefName, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String prefName, String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }
}
