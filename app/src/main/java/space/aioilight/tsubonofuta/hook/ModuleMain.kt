package space.aioilight.tsubonofuta.hook

import android.app.Instrumentation
import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.ConfigResolver
import space.aioilight.tsubonofuta.util.Logger


class ModuleMain : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "Futa-ModuleMain"
        private const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
    }

    private var hooked = false

    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpParam?.packageName ?: return
        if (packageName != PACKAGE_NAME)
            return

        Logger.i(TAG, "Start application hook")
        try {
            XposedBridge.hookAllMethods(
                Instrumentation::class.java,
                "newApplication",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            Logger.i(TAG, "New application created")
                            startHook(param.result as Context, lpParam)
                        } catch (e: Exception) {
                            Logger.w(TAG, e)
                        }
                    }
                })
        } catch (e: Exception) {
            Logger.w(TAG, e)
        }
    }

    private fun startHook(context: Context, lpParam: XC_LoadPackage.LoadPackageParam) {
        if (hooked) {
            Logger.i(TAG, "Application is already hooked")
            return
        }
        hooked = true

        val config = ConfigResolver(context)
        arrayOf(
            NativeAdRemover(),
            UserAgentReplacer(),
            MonaKeyRemover(),
        ).forEach { hook ->
            hook.register(config, lpParam)
        }
    }
}