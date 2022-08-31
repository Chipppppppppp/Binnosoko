package space.aioilight.tsubonofuta.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import space.aioilight.tsubonofuta.config.ConfigResolver
import space.aioilight.tsubonofuta.config.ConfigStore

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {
    protected lateinit var configResolver: ConfigResolver
    private var configStore: ConfigStore? = null
        set(value) {
            preferenceManager.preferenceDataStore = value
            field = value
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        configResolver = ConfigResolver(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        configStore = onLoadConfigStore()
    }

    override fun onPause() {
        configStore?.let { configStore ->
            onSaveConfigStore(configStore)
        }
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.title = preferenceScreen.title
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun onLoadConfigStore(): ConfigStore
    abstract fun onSaveConfigStore(configStore: ConfigStore)
}