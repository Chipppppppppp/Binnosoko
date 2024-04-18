package io.github.chipppppppppp.binnosoko.hook

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Process
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.R
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

        val xPrefs = XSharedPreferences(
            ModuleMain.PACKAGE_NAME,
            "${ModuleMain.MODULE_NAME}-config"
        )

        val classLoader = lpParam.classLoader
        val adClass = XposedHelpers.findClassIfExists(
            xPrefs.getString("adClass", ""),
            classLoader
        )

        Logger.i(TAG, "NativeAdRemover starting")
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
                    override fun afterHookedMethod(param: MethodHookParam) {
                        var view = param.thisObject as View
                        if (view::class.java == adClass) {
                            view.layoutParams.height = 0
                        }
                    }
                }
            )
            var flag = false
            XposedBridge.hookAllMethods(
                View::class.java,
                "onSizeChanged",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (flag) return
                        val view = param.thisObject as View
                        if (param.args[1] == 53 && view is FrameLayout) {
                            flag = true
                            if (view == adClass) return

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

                            prefs.edit().putString("adClass", view.javaClass.name).commit()
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
                }
            )
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}