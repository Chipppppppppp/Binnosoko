package io.github.chipppppppppp.binnosoko.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.Config

interface IHook {
    fun register(config: Config, lpParam: XC_LoadPackage.LoadPackageParam)
}