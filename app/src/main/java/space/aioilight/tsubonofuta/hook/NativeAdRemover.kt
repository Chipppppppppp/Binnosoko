package space.aioilight.tsubonofuta.hook

import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
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
        if (!mainConfig.hideAd) {
            Logger.i(TAG, "NativeAdRemover disabled")
            return
        }

        val internalConfig = config.internalConfig
        val classLoader = lpParam.classLoader
        val adClass = XposedHelpers.findClassIfExists(
            internalConfig.adClass,
            classLoader
        )
        if (adClass == null) {
            Logger.i(TAG, "NativeAdRemover failed: Class not found")
            return
        }

        Logger.i(TAG, "NativeAdRemover starting")
        fun isTargetView(view: View): Boolean {
            return view::class.java == adClass
        }
        try {
            XposedBridge.hookAllMethods(
                classLoader.loadClass("com.amazon.device.ads.DTBAdRequest"),
                "loadAd",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = null
                    }
                }
            )
            XposedBridge.hookAllMethods(
                View::class.java,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as View
                        if (isTargetView(view)) {
                            view.layoutParams.height = 0
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}