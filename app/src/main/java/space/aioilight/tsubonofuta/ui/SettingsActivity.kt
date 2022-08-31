package space.aioilight.tsubonofuta.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import com.nonnonstop.tsubonofuta.BuildConfig
import com.nonnonstop.tsubonofuta.R
import space.aioilight.tsubonofuta.config.ConfigStore

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

    class SettingsFragment : BaseSettingsFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onLoadConfigStore(): ConfigStore {
            return ConfigStore.from(configResolver.mainConfig)
        }

        override fun onSaveConfigStore(configStore: ConfigStore) {
            configResolver.mainConfig = configStore.convert()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            super.onCreatePreferences(savedInstanceState, rootKey)
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("status")?.title = resources.getString(
                R.string.settings_status_title,
                resources.getString(R.string.app_name),
                BuildConfig.VERSION_NAME
            )
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.settings_activity, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.advanced -> {
                    parentFragmentManager.commit {
                        replace(R.id.settings, InternalSettingsFragment())
                        addToBackStack(null)
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    class InternalSettingsFragment : BaseSettingsFragment() {
        override fun onLoadConfigStore(): ConfigStore {
            return ConfigStore.from(configResolver.internalConfig)
        }

        override fun onSaveConfigStore(configStore: ConfigStore) {
            configResolver.internalConfig = configStore.convert()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            super.onCreatePreferences(savedInstanceState, rootKey)
            setPreferencesFromResource(R.xml.advanced_preferences, rootKey)
        }
    }
}