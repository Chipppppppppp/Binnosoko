package space.aioilight.tsubonofuta.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.AppConfig

class UserAgentReplacer(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    fun register() {
        try {
            if (!config[AppConfig.Booleans.REPLACE_USER_AGENT]) {
                XposedBridge.log("UserAgentReplacer disabled")
                return
            }

            XposedBridge.log("UserAgentReplacer starting")
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