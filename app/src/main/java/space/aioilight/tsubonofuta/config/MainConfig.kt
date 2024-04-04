package space.aioilight.tsubonofuta.config

import kotlinx.serialization.Serializable

@Serializable
data class MainConfig(
    var lastVersion: Int = 0,
    var hideAd: Boolean = true,
    var replaceUserAgent: Boolean = false,
    var userAgent: String = "Dalvik/2.1.0 (Linux; U; Android 4.0.3; HT-01 Build/XYZ0.123456.789)",
    var removeMonaKey: Boolean = false,
)