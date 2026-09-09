package io.github.areteruhiro.chmate.haiagaru.ui.hook

import android.app.Application
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
import io.github.areteruhiro.chmate.haiagaru.ui.R
import io.github.areteruhiro.chmate.haiagaru.ui.config.Config
class BannerAdRemover : IHook {
    companion object {
        private const val VERSION_0_8_10_243 = "0.8.10.243"
        private const val AD_CLASS_0_8_10_243 = "o.zzewp"

        @Volatile
        private var seen = false

        @Volatile
        private var adClassName: String? = null

        @Volatile
        private var useKnownAdClass = false
    }
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.hideAd) return

        val xPrefs = XSharedPreferences(
            ModuleMain.PACKAGE_NAME,
            "${ModuleMain.MODULE_NAME}-config"
        )

        val classLoader = lpParam.classLoader
        adClassName = xPrefs.getString("adClass", "")?.takeIf { it.isNotBlank() }

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val context = param.args.getOrNull(0) as? Context ?: return
                    useKnownAdClass = getVersionName(context) == VERSION_0_8_10_243
                    if (useKnownAdClass) {
                        adClassName = AD_CLASS_0_8_10_243
                    }
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
        XposedBridge.hookAllMethods(
            View::class.java,
            "onAttachedToWindow",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as View
                    if (view::class.java.name == adClassName) {
                        view.layoutParams.height = 0
                    }
                }
            }
        )
            XposedBridge.hookAllMethods(
                classLoader.loadClass("androidx.fragment.app.Fragment"),
                "onViewCreated",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context = (param.thisObject as? Any)?.let { obj ->
                            try {
                                obj.javaClass
                                    .getMethod("getContext")
                                    .invoke(obj) as? Context
                            } catch (_: Throwable) {
                                null
                            }
                        }
                            ?: (param.args.getOrNull(0) as? View)?.context
                            ?: return
                        val versionCode = try {
                            val pm = context.packageManager
                            val pi = pm.getPackageInfo(context.packageName, 0)
                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                pi.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                pi.versionCode.toLong()
                            }
                        } catch (t: Throwable) {
                            return
                        }
                        if (useKnownAdClass) return
                        val target = 494L
                        val result = versionCode >= target
                        if (!result) {
                            if (seen) return
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
                            mAddAddAssertPath.invoke(
                                context.resources.assets,
                                ModuleMain.MODULE_PATH
                            )

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
                        }else{
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
                                        val context = container.context
                                        val prefs = context.getSharedPreferences(
                                            "${ModuleMain.MODULE_NAME}-config",
                                            Context.MODE_PRIVATE
                                        )

                                        val savedAdClass = prefs.getString("adClass", null)
                                        if (savedAdClass != null && savedAdClass == adViewClassName) {
                                            return
                                        }
                                        prefs.edit()
                                            .putString("adClass", adViewClassName)
                                            .commit()

                                        seen = true
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
                                        } catch (_: Throwable) {
                                        }
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
                }
            )
    }

    private fun getVersionName(context: Context): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Throwable) {
            null
        }
    }
}
