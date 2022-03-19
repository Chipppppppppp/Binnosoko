package space.aioilight.tsubonofuta

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class UserAgentReplacer(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    fun register() {
        try {
            if (!config[AppConfig.Booleans.REPLACE_USER_AGENT]) {
                XposedBridge.log("Not replace User-Agent")
                return
            }

            XposedBridge.log("Start UserAgentReplacer")
            val userAgent = config[AppConfig.Strings.USER_AGENT]
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
                            XposedBridge.log(e)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }
}