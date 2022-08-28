package space.aioilight.tsubonofuta.hook

import android.app.AndroidAppHelper
import android.content.Context
import androidx.core.content.edit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.AppConfig

class MonaKeyRemover(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    private val classLoader = lpParam.classLoader
    private val cookieClass = XposedHelpers.findClassIfExists(
        config[AppConfig.Strings.CLASS_COOKIE],
        classLoader
    )

    fun register() {
        try {
            if (!config[AppConfig.Booleans.REMOVE_API_ID]) {
                XposedBridge.log("MonaKeyRemover disabled")
                return
            }
            if (cookieClass == null) {
                XposedBridge.log("MonaKeyRemover failed: Class not found")
                return
            }

            XposedBridge.log("MonaKeyRemover starting")
            val prefApiName = config[AppConfig.Strings.PREF_API_NAME]
            val prefApiIdKey = config[AppConfig.Strings.PREF_API_ID_KEY]
            XposedHelpers.findMethodsByExactParameters(
                cookieClass,
                Void.TYPE,
            ).forEach {
                XposedBridge.hookMethod(
                    it,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam?) {
                            try {
                                val sharedPref = AndroidAppHelper.currentApplication()
                                    .getSharedPreferences(prefApiName, Context.MODE_PRIVATE)
                                sharedPref.edit {
                                    remove(prefApiIdKey)
                                }
                                XposedBridge.log("Cookie cleared")
                            } catch (e: Exception) {
                                XposedBridge.log(e)
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }
}