package space.aioilight.tsubonofuta

import android.app.Activity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class VideoAdRemover(private val config: AppConfig, lpParam: XC_LoadPackage.LoadPackageParam) {
    companion object {
        private const val MANAGER_CLASS = "jp.supership.vamp.VAMPManager"
        private const val REQUEST_CLASS = "jp.supership.vamp.VAMPRequest"
        private const val KEY_ACTIVITY = "tsubonofuta.VideoAdRemover.activity"
        private const val KEY_CALLBACK = "tsubonofuta.VideoAdRemover.callback"
    }

    private val classLoader = lpParam.classLoader
    private val managerClass = XposedHelpers.findClass(MANAGER_CLASS, classLoader)
    private val requestClass = XposedHelpers.findClass(REQUEST_CLASS, classLoader)

    fun register() {
        if (!config[AppConfig.Booleans.HIDE_PAST_LOG_AD]) {
            XposedBridge.log("Hide no video ad")
            return
        }

        XposedBridge.log("Start VideoAdRemover")
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
                            XposedBridge.log(e)
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
                                            XposedHelpers.callMethod(callback, "onOpened")
                                            XposedHelpers.callMethod(callback, "onCompleted")
                                        } catch (e: Exception) {
                                            XposedBridge.log(e)
                                        }
                                    }
                                }.start()
                                param.result = null
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