package io.github.chipppppppppp.binnosoko.hook

import android.app.AndroidAppHelper
import android.content.Context
import androidx.core.content.edit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.ConfigResolver
import io.github.chipppppppppp.binnosoko.util.Logger

class MonaKeyRemover : IHook {
    companion object {
        private const val TAG = "Futa-MonaKeyRemover"
    }

    override fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        try {
            val mainConfig = config.mainConfig
            if (!mainConfig.removeMonaKey) {
                Logger.i(TAG, "MonaKeyRemover disabled")
                return
            }

            val internalConfig = config.internalConfig
            val classLoader = lpParam.classLoader
            val cookieClass = XposedHelpers.findClassIfExists(
                internalConfig.cookieClass,
                classLoader
            )
            if (cookieClass == null) {
                Logger.i(TAG, "MonaKeyRemover failed: Class not found")
                return
            }

            Logger.i(TAG, "MonaKeyRemover starting")
            val prefFile = internalConfig.prefMonaKeyFile
            val prefKey = internalConfig.prefMonaKeyName
            XposedHelpers.findMethodsByExactParameters(
                cookieClass,
                Void.TYPE,
            ).forEach {
                XposedBridge.hookMethod(
                    it,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam?) {
                            try {
                                AndroidAppHelper.currentApplication()
                                    .getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                                    .edit {
                                        remove(prefKey)
                                    }
                                Logger.i(TAG, "Cookie cleared")
                            } catch (e: Exception) {
                                Logger.w(TAG, e)
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }
}