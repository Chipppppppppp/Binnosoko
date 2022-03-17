package space.aioilight.tsubonofuta

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import de.robv.android.xposed.XSharedPreferences
import java.io.File
import java.lang.Exception
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AppConfig {
    var hideInlineAd by BooleanPreference("inline", true)
    var hideThreadAd by BooleanPreference("thread", true)

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

    private open class BasePreference<T>(
        private val name: String,
        private val defaultValue: T,
        private val getter: SharedPreferences.(String, T) -> T,
        private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ) : ReadWriteProperty<AppConfig, T> {
        override fun getValue(thisRef: AppConfig, property: KProperty<*>): T {
            return try {
                thisRef.pref.getter(name, defaultValue)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultValue
            }
        }

        @SuppressLint("CommitPrefEdits")
        override fun setValue(thisRef: AppConfig, property: KProperty<*>, value: T) {
            thisRef.pref.edit().setter(name, value).commit()
            makeWorldReadable(thisRef)
        }

        @SuppressLint("SetWorldReadable")
        private fun makeWorldReadable(thisRef: AppConfig) {
            if (!thisRef.privateMode)
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
    }

    private class BooleanPreference(name: String, defaultValue: Boolean) :
        BasePreference<Boolean>(
            name,
            defaultValue,
            SharedPreferences::getBoolean,
            SharedPreferences.Editor::putBoolean
        )
}