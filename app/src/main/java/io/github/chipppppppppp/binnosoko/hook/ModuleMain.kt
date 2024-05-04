package io.github.chipppppppppp.binnosoko.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import io.github.chipppppppppp.binnosoko.ui.AddSettings

class ModuleMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
        const val MODULE_NAME = "io.github.chipppppppppp.binnosoko"
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
            replaceUserAgent = xPrefs.getBoolean("replaceUserAgent", config.replaceUserAgent),
            userAgent = xPrefs.getString("userAgent", config.userAgent) ?: config.userAgent,
            removeMonaKey = xPrefs.getBoolean("removeMonaKey", config.removeMonaKey),
            cookieClass = xPrefs.getString("cookieClass", config.cookieClass) ?: config.cookieClass,
            prefMonaKeyFile = xPrefs.getString("prefMonaKeyFile", config.prefMonaKeyFile) ?: config.prefMonaKeyFile,
            prefMonaKeyName = xPrefs.getString("prefMonaKeyName", config.prefMonaKeyName) ?: config.prefMonaKeyName
        )

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
