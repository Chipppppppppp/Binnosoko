package space.aioilight.tsubonofuta.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.ConfigResolver

interface IHook {
    fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    )
}