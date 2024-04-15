package io.github.chipppppppppp.binnosoko.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config
import io.github.chipppppppppp.binnosoko.util.Logger

class UserAgentReplacer : IHook {
    companion object {
        private const val TAG = "Futa-UserAgentReplacer"
    }

    override fun register(
        config: Config,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        try {
            if (!config.replaceUserAgent) {
                Logger.i(TAG, "UserAgentReplacer disabled")
                return
            }

            Logger.i(TAG, "UserAgentReplacer starting")
            val userAgent = config.userAgent
            XposedHelpers.findAndHookMethod(
                System::class.java,
                "getProperty",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            if (param.args[0] as String != "http.agent")
                                return
                            param.result = userAgent
                        } catch (e: Exception) {
                            Logger.w(TAG, e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}