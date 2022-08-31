package space.aioilight.tsubonofuta.hook

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.ConfigResolver
import space.aioilight.tsubonofuta.util.Logger

class NativeAdRemover : IHook {
    companion object {
        private const val TAG = "Futa-NativeAdRemover"
    }

    override fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        val mainConfig = config.mainConfig
        if (!mainConfig.hideInlineAd && !mainConfig.hideThreadAd) {
            Logger.i(TAG, "NativeAdRemover disabled")
            return
        }

        val internalConfig = config.internalConfig
        val classLoader = lpParam.classLoader
        val inlineAdClass = XposedHelpers.findClassIfExists(
            internalConfig.inlineClass,
            classLoader
        )
        val headAdClass = XposedHelpers.findClassIfExists(
            internalConfig.threadClass,
            classLoader
        )
        val targetActivityClass = XposedHelpers.findClassIfExists(
            internalConfig.adActivityClass,
            classLoader
        )
        if (inlineAdClass == null && headAdClass == null) {
            Logger.i(TAG, "NativeAdRemover failed: Class not found")
            return
        }

        Logger.i(TAG, "NativeAdRemover starting")
        fun isTargetView(view: View): Boolean {
            val cls = view::class.java
            if (mainConfig.hideInlineAd && cls == inlineAdClass) {
                return true
            }
            if (mainConfig.hideThreadAd && cls == headAdClass) {
                if (targetActivityClass == null || view.context::class.java == targetActivityClass) {
                    return true
                }
            }
            return false
        }
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
                            Logger.w(TAG, e)
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
                            Logger.w(TAG, e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}