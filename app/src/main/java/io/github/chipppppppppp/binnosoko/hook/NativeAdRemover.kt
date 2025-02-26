package io.github.chipppppppppp.binnosoko.hook

import android.app.Fragment
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
import java.util.Timer
import kotlin.concurrent.timerTask

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
            classLoader.loadClass("androidx.fragment.app.Fragment"),
            "onViewCreated",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (seen) return
                    if (param.args[0] !is ViewGroup) return
                    val viewGroup = param.args[0] as ViewGroup
                    if (viewGroup.childCount < 3) return
                    val adView = viewGroup.getChildAt(viewGroup.childCount - 3)
                    if (adView !is FrameLayout) return
                    seen = true
                    if (adView::class.java == adClass) return

                    val context = viewGroup.context
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
                }
            }
        )

        XposedBridge.hookAllMethods(
            View::class.java,
            "onAttachedToWindow",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View
                    if (view::class.java == adClass) {
                        view.layoutParams.height = 0
                    }
                }
            }
        )
    }
}