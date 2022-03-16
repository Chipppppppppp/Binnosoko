package space.aioilight.tsubonofuta

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class layP : IXposedHookLoadPackage {
    companion object {
        private const val PACKAGE_NAME = "jp.co.airfront.android.a2chMate"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val packageName = lpparam?.packageName ?: return
        if (packageName != PACKAGE_NAME)
            return

        val config = AppConfig()
        if (!config.isEnabled()) {
            XposedBridge.log("Hide no ad")
            return
        }

        XposedBridge.log("Start hook")
        try {
            doHook(config, lpparam.classLoader)
        } catch (e: Exception) {
            XposedBridge.log("Cannot hook method of thread ad.")
            XposedBridge.log(e.toString())
        }
    }

    private fun doHook(config: AppConfig, classLoader: ClassLoader) {
        val targetChecker = TargetChecker(config, classLoader)

        XposedHelpers.findAndHookMethod(
            ViewGroup::class.java,
            "onViewAdded",
            View::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as ViewGroup
                    if (!targetChecker.isTargetView(view))
                        return
                    view.removeAllViews()
                    param.result = null
                }
            }
        )

        val methodSetMeasuredDimension = XposedHelpers.findMethodExact(
            View::class.java,
            "setMeasuredDimension",
            Int::class.java,
            Int::class.java
        )
        XposedHelpers.findAndHookMethod(
            FrameLayout::class.java,
            "onMeasure",
            Int::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as ViewGroup
                    if (!targetChecker.isTargetView(view))
                        return
                    methodSetMeasuredDimension.invoke(view, 0, 0)
                    param.result = null
                }
            }
        )
    }
}