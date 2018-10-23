package tracebrace.tracebrace_final;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

    TinyDB localDb;
    Settings settings;
    ListPreference message_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //init
        settings = new Settings();
        localDb = new TinyDB(this);

        //settings.gps_tracking = gps_tracking.isEnabled();
        //sharedPref.edit().putBoolean("gps_allow", false).apply();

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                            if(sharedPref.getBoolean("gps_allow", false) && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
                                ActivityCompat.requestPermissions(SettingsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        1);
                            }
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(spChanged);
    }
}
