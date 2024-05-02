package io.github.chipppppppppp.binnosoko.hook

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Process
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.R
import io.github.chipppppppppp.binnosoko.config.Config

class NativeAdRemover : IHook {
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.hideAd) return

        val xPrefs = XSharedPreferences(
            ModuleMain.PACKAGE_NAME,
            "${ModuleMain.MODULE_NAME}-config"
        )

        val classLoader = lpParam.classLoader
        val adClass = XposedHelpers.findClassIfExists(
            xPrefs.getString("adClass", ""),
            classLoader
        )
        val adParentClass = XposedHelpers.findClassIfExists(
            xPrefs.getString("adParentClass", "o.setPreserveFocusAfterLayout"),
            classLoader
        )

        XposedBridge.hookAllMethods(
            classLoader.loadClass("com.amazon.device.ads.DTBAdRequest"),
            "loadAd",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = null
                }
            }
        )
        var seen = false
        XposedBridge.hookAllMethods(
            View::class.java,
            "onAttachedToWindow",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View
                    if (view::class.java == adParentClass) {
                        if (seen) return
                        val adParent = view as ViewGroup
                        if (adParent.childCount < 3) return
                        val adView = adParent.getChildAt(adParent.childCount - 3)
                        if (adView !is FrameLayout) return
                        seen = true
                        if (adView::class.java == adClass) return

                        val context = view.context
                        val mAddAddAssertPath =
                            AssetManager::class.java.getDeclaredMethod(
                                "addAssetPath",
                                String::class.java
                            )
                        mAddAddAssertPath.isAccessible = true
                        mAddAddAssertPath.invoke(context.resources.assets, ModuleMain.MODULE_PATH)

                        val prefs = context.getSharedPreferences(
                            "${ModuleMain.MODULE_NAME}-config",
                            Context.MODE_PRIVATE
                        )
                        prefs.edit().putString("adClass", adView.javaClass.name).commit()

                        Toast.makeText(
                            context.applicationContext,
                            context.getString(R.string.restarting),
                            Toast.LENGTH_SHORT
                        ).show()
                        Process.killProcess(Process.myPid())
                        context.startActivity(
                            Intent().setClassName(
                                ModuleMain.MODULE_NAME,
                                "jp.syoboi.a2chMate.activity.HomeActivity"
                            )
                        )
                    } else if (view::class.java == adClass) {
                        view.layoutParams.height = 0
                    }
                }
            }
        )
    }
}