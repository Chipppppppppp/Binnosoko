package io.github.chipppppppppp.binnosoko.hook

import android.app.AndroidAppHelper
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config

class MonaKeyRemover : IHook {
    override fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (!config.removeMonaKey) return

        val classLoader = lpParam.classLoader
        val cookieClass = XposedHelpers.findClassIfExists(
            config.cookieClass,
            classLoader
        ) ?: return

        val prefFile = config.prefMonaKeyFile
        val prefKey = config.prefMonaKeyName
        XposedHelpers.findMethodsByExactParameters(
            cookieClass,
            Void.TYPE,
        ).forEach {
            XposedBridge.hookMethod(
                it,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        AndroidAppHelper.currentApplication()
                            .getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                            .edit()
                            .remove(prefKey)
                            .apply()
                    }
                }
            )
        }
    }
}