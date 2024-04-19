package io.github.chipppppppppp.binnosoko.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config

class UserAgentReplacer : IHook {
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.replaceUserAgent) return

        val userAgent = config.userAgent
        XposedHelpers.findAndHookMethod(
            System::class.java,
            "getProperty",
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.args[0] as String != "http.agent") return
                    param.result = userAgent
                }
            }
        )
    }
}