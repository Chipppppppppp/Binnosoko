package space.aioilight.tsubonofuta

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nonnonstop.tsubonofuta.BuildConfig
import com.nonnonstop.tsubonofuta.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportFragmentManager.addOnBackStackChangedListener {
            supportActionBar?.setDisplayHomeAsUpEnabled(
                supportFragmentManager.backStackEntryCount > 0
            )
        }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.settings, SettingsFragment())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = AppConfig.newInstanceForModule(requireContext())
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("status")?.title = resources.getString(
                R.string.settings_status_title,
                resources.getString(R.string.app_name),
                BuildConfig.VERSION_NAME
            )
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            activity?.title = preferenceScreen.title
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.settings_activity, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.advanced -> {
                    parentFragmentManager.commit {
                        replace(R.id.settings, AdvancedSettingsFragment())
                        addToBackStack(null)
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    class AdvancedSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = AppConfig.newInstanceForModule(requireContext())
            setPreferencesFromResource(R.xml.advanced_preferences, rootKey)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            activity?.title = preferenceScreen.title
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }
}