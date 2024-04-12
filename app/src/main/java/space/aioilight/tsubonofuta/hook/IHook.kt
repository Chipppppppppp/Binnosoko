package space.aioilight.tsubonofuta.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import space.aioilight.tsubonofuta.config.Config

interface IHook {
    fun register(
        config: Config,
        lpParam: XC_LoadPackage.LoadPackageParam
    )
}