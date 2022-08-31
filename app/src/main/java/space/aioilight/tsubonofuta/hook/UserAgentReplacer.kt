package space.aioilight.tsubonofuta.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.ConfigResolver
import space.aioilight.tsubonofuta.util.Logger

class UserAgentReplacer : IHook {
    companion object {
        private const val TAG = "Futa-UserAgentReplacer"
    }

    override fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        try {
            val mainConfig = config.mainConfig
            if (!mainConfig.replaceUserAgent) {
                Logger.i(TAG, "UserAgentReplacer disabled")
                return
            }

            Logger.i(TAG, "UserAgentReplacer starting")
            val userAgent = mainConfig.userAgent
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