package io.github.areteruhiro.chmate.haiagaru.ui.hook

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.areteruhiro.chmate.haiagaru.ui.config.Config
import io.github.areteruhiro.chmate.haiagaru.ui.ui.AddSettings

class ModuleMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
        const val MODULE_NAME = "io.github.areteruhiro.chmate.haiagaru.ui"
        lateinit var MODULE_PATH: String
    }

    private var config = Config()

    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpParam?.packageName ?: return
        if (packageName != PACKAGE_NAME) return

        val xPrefs = XSharedPreferences(
            PACKAGE_NAME,
            "$MODULE_NAME-config"
        )

        config = Config(
            hideAd = xPrefs.getBoolean("hideAd", config.hideAd),
            chtoio = xPrefs.getBoolean("chtoio", config.chtoio),
            replaceUserAgent = xPrefs.getBoolean("replaceUserAgent", config.replaceUserAgent),
            userAgent = xPrefs.getString("userAgent", config.userAgent) ?: config.userAgent,
            removeMonaKey = xPrefs.getBoolean("removeMonaKey", config.removeMonaKey),
            cookieClass = xPrefs.getString("cookieClass", config.cookieClass) ?: config.cookieClass,
            prefMonaKeyFile = xPrefs.getString("prefMonaKeyFile", config.prefMonaKeyFile)
                ?: config.prefMonaKeyFile,
            prefMonaKeyName = xPrefs.getString("prefMonaKeyName", config.prefMonaKeyName)
                ?: config.prefMonaKeyName
        )

        arrayOf(
            AddSettings(),
            BannerAdRemover(),
            InlineAdRemover(),
            UserAgentReplacer(),
            MonaKeyRemover(),
            Chtoio(),
        ).forEach { hook ->
            val hookName = hook::class.simpleName ?: "UnknownHook"
            try {
                hook.register(config, lpParam)
            } catch (e: Exception) {
                val errorMessage =
                    "HookRegister: Failed to register $hookName\n${Log.getStackTraceString(e)}"
                XposedBridge.log(errorMessage)
            }
        }
    }

    override fun initZygote(startupParam: StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }
}
