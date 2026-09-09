package io.github.areteruhiro.chmate.haiagaru.ui.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.areteruhiro.chmate.haiagaru.ui.config.Config

interface IHook {
    fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam)
}
