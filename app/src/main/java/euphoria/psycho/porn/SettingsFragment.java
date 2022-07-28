package euphoria.psycho.porn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {


    public static final String KEY_VIDEO_FOLDER = "key_video_folder";
    public static final String KEY_CK_COOKIE = "key_ck_cookie";
    public static final String KEY_91_COOKIE = "key_91_cookie";
    // USER_AGENT user-agent
    public static final String KEY_USER_AGENT = "user-agent";

    public static void setString(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static String getString(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defValue);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

}
