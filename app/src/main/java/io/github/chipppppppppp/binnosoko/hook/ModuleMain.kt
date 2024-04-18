package io.github.chipppppppppp.binnosoko.hook

import android.app.Instrumentation
import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import io.github.chipppppppppp.binnosoko.ui.AddSettings
import io.github.chipppppppppp.binnosoko.util.Logger

class ModuleMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        private const val TAG = "Futa-ModuleMain"
        const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
        const val MODULE_NAME = "space.aioilight.tsubonofuta"
        lateinit var MODULE_PATH: String
    }

    private var hooked = false
    private var config = Config()

    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpParam?.packageName ?: return
        if (packageName != PACKAGE_NAME)
            return

        val xPrefs = XSharedPreferences(
            PACKAGE_NAME,
            "$MODULE_NAME-config"
        )

        config = Config(
            hideAd = xPrefs.getBoolean("hideAd", config.hideAd),
            adHeight = xPrefs.getInt("adHeight", config.adHeight),
            replaceUserAgent = xPrefs.getBoolean("replaceUserAgent", config.replaceUserAgent),
            userAgent = xPrefs.getString("userAgent", config.userAgent) ?: config.userAgent,
            removeMonaKey = xPrefs.getBoolean("removeMonaKey", config.removeMonaKey),
            cookieClass = xPrefs.getString("cookieClass", config.cookieClass) ?: config.cookieClass,
            prefMonaKeyFile = xPrefs.getString("prefMonaKeyFile", config.prefMonaKeyFile) ?: config.prefMonaKeyFile,
            prefMonaKeyName = xPrefs.getString("prefMonaKeyName", config.prefMonaKeyName) ?: config.prefMonaKeyName
        )

        Logger.i(TAG, "Start application hook")
        try {
            XposedBridge.hookAllMethods(
                Instrumentation::class.java,
                "newApplication",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            Logger.i(TAG, "New application created")
                            startHook(param.result as Context, lpParam)
                        } catch (e: Exception) {
                            Logger.w(TAG, e)
                        }
                    }
                })
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }

    private fun startHook(context: Context, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (hooked) {
            Logger.i(TAG, "Application is already hooked")
            return
        }
        hooked = true

        arrayOf(
            AddSettings(),
            NativeAdRemover(),
            UserAgentReplacer(),
            MonaKeyRemover(),
        ).forEach { hook ->
            hook.register(config, lpParam)
        }
    }

    override fun initZygote(startupParam: StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }
}