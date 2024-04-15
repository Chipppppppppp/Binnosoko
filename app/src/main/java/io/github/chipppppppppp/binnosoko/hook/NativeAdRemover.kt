package io.github.chipppppppppp.binnosoko.hook

import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import io.github.chipppppppppp.binnosoko.util.Logger

class NativeAdRemover : IHook {
    companion object {
        private const val TAG = "Futa-NativeAdRemover"
    }

    override fun register(
        config: Config,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        if (!config.hideAd) {
            Logger.i(TAG, "NativeAdRemover disabled")
            return
        }

        val classLoader = lpParam.classLoader
        val adClass = XposedHelpers.findClassIfExists(
            config.adClass,
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
                        var view = param.thisObject as View
                        if (isTargetView(view)) {
                            XposedBridge.log("Called!")
                            XposedBridge.log(view.toString())
                            XposedBridge.log(view.height.toString() + ", " + view.width.toString())
                            view.layoutParams.height = 0
                            while (view.parent != null && view.parent is View) {
                                view = view.parent as View
                                XposedBridge.log(view.toString())
                                XposedBridge.log(view.height.toString() + ", " + view.width.toString())
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}