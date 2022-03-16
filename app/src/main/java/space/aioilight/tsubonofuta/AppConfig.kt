package space.aioilight.tsubonofuta

import de.robv.android.xposed.XSharedPreferences

class AppConfig {
    val hideInlineAd: Boolean
    val hideThreadAd: Boolean

    init {
        val pref = XSharedPreferences(BuildConfig.APPLICATION_ID, "tsuboprefs")
        hideInlineAd = pref.getBoolean("inline", true)
        hideThreadAd = pref.getBoolean("thread", true)
    }

    fun isEnabled(): Boolean {
        return hideInlineAd || hideThreadAd
    }
}