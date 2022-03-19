package space.aioilight.tsubonofuta

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceDataStore
import de.robv.android.xposed.XSharedPreferences
import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AppConfig : PreferenceDataStore {
    var hideInlineAd by BooleanPreference("inline", true)
    var hideThreadAd by BooleanPreference("thread", true)
    var hidePastLogAd by BooleanPreference("past_log", true)

    private val pref: SharedPreferences
    private var privateMode = false

    companion object {
        private const val FILE_NAME = "tsuboprefs"

        fun newInstanceForHookedApp(): AppConfig {
            return AppConfig()
        }

        fun newInstanceForModule(context: Context): AppConfig {
            return AppConfig(context)
        }
    }

    private constructor() {
        pref = XSharedPreferences(BuildConfig.APPLICATION_ID, FILE_NAME)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WorldReadableFiles")
    private constructor(context: Context) {
        pref = try {
            context.getSharedPreferences(FILE_NAME, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            privateMode = true
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    @SuppressLint("SetWorldReadable")
    private fun makeWorldReadable() {
        if (!privateMode)
            return

        try {
            val packageDir = File(
                Environment.getDataDirectory(),
                "data/${BuildConfig.APPLICATION_ID}/"
            )
            val prefDir = File(
                packageDir,
                "shared_prefs/"
            )
            val prefFile = File(
                prefDir,
                "${FILE_NAME}.xml"
            )
            prefFile.setReadable(true, false)
            prefDir.setExecutable(true, false)
            packageDir.setExecutable(true, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun putString(key: String, value: String?) {
        pref.edit().putString(key, value).commit()
        makeWorldReadable()
    }

    @SuppressLint("ApplySharedPref")
    override fun putStringSet(key: String, values: MutableSet<String>?) {
        pref.edit().putStringSet(key, values).commit()
        makeWorldReadable()
    }

    @SuppressLint("ApplySharedPref")
    override fun putInt(key: String, value: Int) {
        pref.edit().putInt(key, value).commit()
        makeWorldReadable()
    }

    @SuppressLint("ApplySharedPref")
    override fun putLong(key: String, value: Long) {
        pref.edit().putLong(key, value).commit()
        makeWorldReadable()
    }

    @SuppressLint("ApplySharedPref")
    override fun putFloat(key: String, value: Float) {
        pref.edit().putFloat(key, value).commit()
        makeWorldReadable()
    }

    @SuppressLint("ApplySharedPref")
    override fun putBoolean(key: String, value: Boolean) {
        pref.edit().putBoolean(key, value).commit()
        makeWorldReadable()
    }

    override fun getString(key: String, defValue: String?): String? {
        return pref.getString(key, defValue)
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return pref.getStringSet(key, defValues)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return pref.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return pref.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return pref.getBoolean(key, defValue)
    }

    private sealed class BasePreference<T>(
        private val name: String,
        private val defaultValue: T,
        private val getter: AppConfig.(String, T) -> T,
        private val setter: AppConfig.(String, T) -> Unit
    ) : ReadWriteProperty<AppConfig, T> {
        override fun getValue(thisRef: AppConfig, property: KProperty<*>): T {
            return try {
                thisRef.getter(name, defaultValue)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultValue
            }
        }

        @SuppressLint("CommitPrefEdits")
        override fun setValue(thisRef: AppConfig, property: KProperty<*>, value: T) {
            try {
                thisRef.setter(name, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private class BooleanPreference(name: String, defaultValue: Boolean) :
        BasePreference<Boolean>(
            name,
            defaultValue,
            AppConfig::getBoolean,
            AppConfig::putBoolean
        )
}