package space.aioilight.tsubonofuta.hook

import android.app.Activity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.ConfigResolver
import space.aioilight.tsubonofuta.util.Logger

class VideoAdRemover : IHook {
    companion object {
        private const val TAG = "Futa-VideoAdRemover"
        private const val KEY_ACTIVITY = "tsubonofuta.VideoAdRemover.activity"
        private const val KEY_CALLBACK = "tsubonofuta.VideoAdRemover.callback"
    }

    override fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    ) {
        val mainConfig = config.mainConfig
        if (!mainConfig.hideLogAd) {
            Logger.i(TAG, "VideoAdRemover disabled")
            return
        }

        val internalConfig = config.internalConfig
        val classLoader = lpParam.classLoader
        val managerClass = XposedHelpers.findClassIfExists(
            internalConfig.videoManagerClass,
            classLoader
        )
        val requestClass = XposedHelpers.findClassIfExists(
            internalConfig.videoRequestClass,
            classLoader
        )
        val methodCallbackOpen = internalConfig.videoOpenMethod
        val methodCallbackComplete = internalConfig.videoCompleteMethod
        if (managerClass == null || requestClass == null) {
            Logger.i(TAG, "VideoAdRemover failed: Class not found")
            return
        }

        Logger.i(TAG, "VideoAdRemover starting")
        try {
            XposedBridge.hookMethod(
                XposedHelpers.findConstructorBestMatch(
                    managerClass,
                    Activity::class.java,
                    String::class.java,
                    null,
                ),
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            XposedHelpers.setAdditionalInstanceField(
                                param.thisObject,
                                KEY_ACTIVITY,
                                param.args[0]
                            )
                            XposedHelpers.setAdditionalInstanceField(
                                param.thisObject,
                                KEY_CALLBACK,
                                param.args[2]
                            )
                        } catch (e: Exception) {
                            Logger.w(TAG, e)
                        }
                    }
                }
            )

            XposedHelpers.findMethodsByExactParameters(
                managerClass,
                Void.TYPE,
                requestClass,
            ).forEach {
                XposedBridge.hookMethod(
                    it,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            try {
                                val activity = XposedHelpers.getAdditionalInstanceField(
                                    param.thisObject,
                                    KEY_ACTIVITY
                                ) as Activity
                                val callback = XposedHelpers.getAdditionalInstanceField(
                                    param.thisObject,
                                    KEY_CALLBACK
                                )
                                Thread {
                                    Thread.sleep(5000)
                                    activity.runOnUiThread {
                                        try {
                                            XposedHelpers.callMethod(
                                                callback,
                                                methodCallbackOpen
                                            )
                                            XposedHelpers.callMethod(
                                                callback,
                                                methodCallbackComplete
                                            )
                                        } catch (e: Exception) {
                                            Logger.w(TAG, e)
                                        }
                                    }
                                }.start()
                                param.result = null
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