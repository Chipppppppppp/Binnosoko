package io.github.chipppppppppp.binnosoko.config

data class Config(
    var hideAd: Boolean = true,
    var adHeight: Int = 53,
    var replaceUserAgent: Boolean = false,
    var userAgent: String = "Dalvik/2.1.0 (Linux; U; Android 4.0.3; HT-01 Build/XYZ0.123456.789)",
    var removeMonaKey: Boolean = false,
    var cookieClass: String = "com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor",
    var prefMonaKeyFile: String = "2chapi",
    var prefMonaKeyName: String = "2chapi_monakey"
)