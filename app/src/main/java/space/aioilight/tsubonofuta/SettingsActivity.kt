package space.aioilight.tsubonofuta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nonnonstop.tsubonofuta.BuildConfig
import com.nonnonstop.tsubonofuta.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val config = AppConfig.newInstanceForModule(requireContext())
            preferenceManager.preferenceDataStore = config
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("status")?.title = resources.getString(
                R.string.settings_status_title,
                resources.getString(R.string.app_name),
                BuildConfig.VERSION_NAME
            )
        }
    }
}