package apps.junkuvo.alertapptowalksafely;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceController {
    private Context mContext;

    public SharedPreferenceController(Context context){
        mContext = context;
    }

    public void setIntValue(String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putInt(key, value).commit();
    }

    public void setIsBootedValue(boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putBoolean("Booted", value).commit();
    }

    public int getIntValue(String key) {
        int state;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        state = sp.getInt(key, 0);
        return state;
    }

    public boolean isBooted() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean("Booted",false);
    }
}
