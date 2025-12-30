package io.github.chipppppppppp.binnosoko.hook

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
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

class BannerAdRemover : IHook {
    var seen = false
    private var isHighVersion: Boolean = false
    fun init(context: Context) {
        isHighVersion = isTargetVersion(context)
    }

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

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val app = param.thisObject as Application
                    BannerAdRemover().init(app)
                }
            }
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
        if (!isHighVersion) {

            XposedBridge.hookAllMethods(
                classLoader.loadClass("androidx.fragment.app.Fragment"),
                "onViewCreated",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (seen) {
                            return
                        }
                        if (param.args[0] !is ViewGroup) {
                            return
                        }
                        val viewGroup = param.args[0] as ViewGroup
                        if (viewGroup.childCount < 3) {
                            return
                        }

                        val adView = viewGroup.getChildAt(viewGroup.childCount - 3)
                        val adViewClassName = adView::class.java.name

                        if (adView !is FrameLayout) {
                            return
                        }
                        if (adViewClassName == "android.widget.FrameLayout") {
                            return
                        }

                        seen = true
                        val context = viewGroup.context
                        val prefs = context.getSharedPreferences(
                            "${ModuleMain.MODULE_NAME}-config",
                            Context.MODE_PRIVATE
                        )
                        val savedAdClass = prefs.getString("adClass", null)
                        if (savedAdClass != null && adViewClassName == savedAdClass) {
                            return
                        }

                        val mAddAddAssertPath =
                            AssetManager::class.java.getDeclaredMethod(
                                "addAssetPath",
                                String::class.java
                            )
                        mAddAddAssertPath.isAccessible = true
                        mAddAddAssertPath.invoke(context.resources.assets, ModuleMain.MODULE_PATH)

                        prefs.edit().putString("adClass", adViewClassName).commit()

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

        } else {
            XposedBridge.hookAllMethods(
                classLoader.loadClass("androidx.fragment.app.Fragment"),
                "onViewCreated",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (seen) return

                        val fragment = param.thisObject
                        if (fragment.javaClass.name !=
                            "jp.syoboi.a2chMate.ui.home.HomeFragment"
                        ) return
                        val root = param.args.getOrNull(0) as? ViewGroup ?: return
                        if (root.childCount == 0) return
                        val container = root.getChildAt(0) as? ViewGroup ?: return
                        var hit = 0
                        for (i in 0 until container.childCount) {
                            val child = container.getChildAt(i) ?: continue
                            if (child is FrameLayout) {
                                hit++
                                if (hit == 2) {
                                    val adViewClassName = child.javaClass.name
                                    seen = true
                                    val context = container.context
                                    val prefs = context.getSharedPreferences(
                                        "${ModuleMain.MODULE_NAME}-config",
                                        Context.MODE_PRIVATE
                                    )
                                    prefs.edit()
                                        .putString("adClass", adViewClassName)
                                        .commit()
                                    try {
                                        val mAddAssetPath =
                                            AssetManager::class.java.getDeclaredMethod(
                                                "addAssetPath",
                                                String::class.java
                                            )
                                        mAddAssetPath.isAccessible = true
                                        mAddAssetPath.invoke(
                                            context.resources.assets,
                                            ModuleMain.MODULE_PATH
                                        )
                                    } catch (_: Throwable) {}

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
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                    return
                                }
                            }
                        }
                    }
                }
            )
        }
        XposedBridge.hookAllMethods(
            View::class.java,
            "onAttachedToWindow",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View
                    if (adClass != null && view::class.java == adClass) {
                        view.layoutParams.height = 0
                    }
                }
            }
        )
    }

    fun isTargetVersion(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= 28) {
                pi.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pi.versionCode.toLong()
            }
            val target = 494L
            val result = versionCode >= target
            result
        } catch (t: Throwable) {
            false
        }
    }



}