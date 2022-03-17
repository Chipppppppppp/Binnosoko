package space.aioilight.tsubonofuta

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import de.robv.android.xposed.XSharedPreferences
import java.io.File
import java.lang.Exception

class AppConfig {
    companion object {
        private const val FILE_NAME = "tsuboprefs"
    }

    private val pref: SharedPreferences
    private var privateMode = false
    var hideInlineAd = true
    var hideThreadAd = true

    constructor() {
        pref = XSharedPreferences(BuildConfig.APPLICATION_ID, FILE_NAME)
        if (pref.file.canRead())
            load()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WorldReadableFiles")
    constructor(context: Context) {
        pref = try {
            context.getSharedPreferences(FILE_NAME, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            privateMode = true
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
        load()
    }

    private fun load() {
        hideInlineAd = pref.getBoolean("inline", true)
        hideThreadAd = pref.getBoolean("thread", true)
    }

    @SuppressLint("SetWorldReadable")
    fun save() {
        with(pref.edit()) {
            putBoolean("inline", hideInlineAd)
            putBoolean("thread", hideThreadAd)
            commit()
        }
        if (privateMode) {
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
}