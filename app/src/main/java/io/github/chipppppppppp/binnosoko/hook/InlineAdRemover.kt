package io.github.chipppppppppp.binnosoko.hook

import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config

class InlineAdRemover : IHook {
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.hideAd) return

        val classLoader = lpParam.classLoader

        XposedBridge.hookAllMethods(
            classLoader.loadClass("com.unity3d.mediation.banner.LevelPlayBannerAdView"),
            "loadAd",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = null
                }
            }
        )

        XposedBridge.hookAllConstructors(
            classLoader.loadClass("com.unity3d.mediation.banner.LevelPlayBannerAdView"),
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    (param.thisObject as View).visibility = View.GONE
                }
            }
        )
    }
}