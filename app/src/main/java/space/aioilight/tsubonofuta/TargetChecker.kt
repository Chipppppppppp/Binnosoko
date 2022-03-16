package space.aioilight.tsubonofuta

import android.view.View
import de.robv.android.xposed.XposedHelpers

class TargetChecker(private val config: AppConfig, classLoader: ClassLoader) {
    companion object {
        private const val INLINE_AD_CLASS = "jp.syoboi.a2chMate.view.ad.InlineAdContainer"
        private const val HEAD_AD_CLASS = "jp.syoboi.a2chMate.view.MyAdView"
        private const val TARGET_ACTIVITY = "jp.syoboi.a2chMate.activity.ResListActivity"
    }

    private val inlineAdClass = XposedHelpers.findClass(INLINE_AD_CLASS, classLoader)
    private val headAdClass = XposedHelpers.findClass(HEAD_AD_CLASS, classLoader)
    private val targetActivityClass = XposedHelpers.findClass(TARGET_ACTIVITY, classLoader)

    fun isTargetView(view: View): Boolean {
        if (config.hideInlineAd && view::class.java == inlineAdClass) {
            return true
        }
        if (config.hideThreadAd && view::class.java == headAdClass) {
            if (view.context::class.java == targetActivityClass) {
                return true
            }
        }
        return false
    }
}