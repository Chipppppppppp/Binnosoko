package space.aioilight.tsubonofuta

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceDataStore
import com.nonnonstop.tsubonofuta.BuildConfig
import de.robv.android.xposed.XSharedPreferences
import java.io.File

class AppConfig : PreferenceDataStore {
    enum class Booleans(val prefKey: String, val defaultValue: Boolean) {
        HIDE_INLINE_AD("inline", true),
        HIDE_THREAD_AD("thread", true),
        HIDE_PAST_LOG_AD("past_log", true),
        REPLACE_USER_AGENT("ua_enabled", false),
        REMOVE_API_ID("api_id", false),
    }

    enum class Strings(val prefKey: String, val defaultValue: String) {
        USER_AGENT("ua", System.getProperty("http.agent") ?: ""),
        CLASS_THREAD_AD(
            "thread_cls",
            "jp.syoboi.a2chMate.view.MyAdView"
        ),
        CLASS_INLINE_AD(
            "inline_cls",
            "jp.syoboi.a2chMate.view.ad.InlineAdContainer"
        ),
        CLASS_TARGET_ACTIVITY(
            "noad_target_activity",
            "jp.syoboi.a2chMate.activity.ResListActivity"
        ),
        CLASS_VIDEO_MANAGER(
            "video_manager_cls",
            "jp.supership.vamp.VAMPManager"
        ),
        CLASS_VIDEO_REQUEST(
            "video_request_cls",
            "jp.supership.vamp.VAMPRequest"
        ),
        METHOD_VIDEO_OPEN("video_open_method", "onOpened"),
        METHOD_VIDEO_COMPLETE("video_complete_method", "onCompleted"),
        CLASS_COOKIE(
            "cookie_cls",
            "com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor"
        ),
        PREF_API_NAME("api_name", "2chapi"),
        PREF_API_ID_KEY("api_id_key", "2chapi_monakey"),
    }

    private val pref: SharedPreferences

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
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    @SuppressLint("SetWorldReadable")
    private fun makeWorldReadable() {
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

    // ----- Boolean -----

    override fun putBoolean(key: String, value: Boolean) {
        set(Booleans.valueOf(key), value)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return get(Booleans.valueOf(key))
    }

    @SuppressLint("ApplySharedPref")
    operator fun set(key: Booleans, value: Boolean) {
        pref.edit().putBoolean(key.prefKey, value).commit()
        makeWorldReadable()
    }

    operator fun get(key: Booleans): Boolean {
        return pref.getBoolean(key.prefKey, key.defaultValue)
    }

    // ----- String -----

    override fun putString(key: String, value: String?) {
        set(Strings.valueOf(key), value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return get(Strings.valueOf(key))
    }

    @SuppressLint("ApplySharedPref")
    operator fun set(key: Strings, value: String?) {
        pref.edit().putString(key.prefKey, value).commit()
        makeWorldReadable()
    }

    operator fun get(key: Strings): String? {
        return pref.getString(key.prefKey, key.defaultValue)
    }
}