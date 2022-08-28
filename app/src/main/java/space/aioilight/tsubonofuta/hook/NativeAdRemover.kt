package space.aioilight.tsubonofuta.hook

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.AppConfig

class NativeAdRemover(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    private val classLoader = lpParam.classLoader
    private val inlineAdClass =
        XposedHelpers.findClassIfExists(
            config[AppConfig.Strings.CLASS_INLINE_AD],
            classLoader
        )
    private val headAdClass =
        XposedHelpers.findClassIfExists(
            config[AppConfig.Strings.CLASS_THREAD_AD],
            classLoader
        )
    private val targetActivityClass =
        XposedHelpers.findClassIfExists(
            config[AppConfig.Strings.CLASS_TARGET_ACTIVITY],
            classLoader
        )

    fun register() {
        if (!config[AppConfig.Booleans.HIDE_INLINE_AD] && !config[AppConfig.Booleans.HIDE_THREAD_AD]) {
            XposedBridge.log("NativeAdRemover disabled")
            return
        }
        if (inlineAdClass == null && headAdClass == null) {
            XposedBridge.log("NativeAdRemover failed: Class not found")
            return
        }

        XposedBridge.log("NativeAdRemover starting")
        try {
            XposedHelpers.findAndHookMethod(
                ViewGroup::class.java,
                "onViewAdded",
                View::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.thisObject as ViewGroup
                            if (!isTargetView(view))
                                return
                            view.removeAllViews()
                            param.result = null
                        } catch (e: Exception) {
                            XposedBridge.log(e)
                        }
                    }
                }
            )

            val methodSetMeasuredDimension = XposedHelpers.findMethodExact(
                View::class.java,
                "setMeasuredDimension",
                Int::class.java,
                Int::class.java
            )
            XposedHelpers.findAndHookMethod(
                FrameLayout::class.java,
                "onMeasure",
                Int::class.java,
                Int::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.thisObject as ViewGroup
                            if (!isTargetView(view))
                                return
                            methodSetMeasuredDimension.invoke(view, 0, 0)
                            param.result = null
                        } catch (e: Exception) {
                            XposedBridge.log(e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }

    private fun isTargetView(view: View): Boolean {
        if (config[AppConfig.Booleans.HIDE_INLINE_AD] && view::class.java == inlineAdClass) {
            return true
        }
        if (config[AppConfig.Booleans.HIDE_THREAD_AD] && view::class.java == headAdClass) {
            if (targetActivityClass == null || view.context::class.java == targetActivityClass) {
                return true
            }
        }
        return false
    }
}