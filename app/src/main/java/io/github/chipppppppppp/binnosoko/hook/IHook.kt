package io.github.chipppppppppp.binnosoko.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chipppppppppp.binnosoko.config.ConfigResolver

interface IHook {
    fun register(
        config: ConfigResolver,
        lpParam: XC_LoadPackage.LoadPackageParam
    )
}