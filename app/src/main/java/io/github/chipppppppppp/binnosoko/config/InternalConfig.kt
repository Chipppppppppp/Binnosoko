package io.github.chipppppppppp.binnosoko.config

import kotlinx.serialization.Serializable

@Serializable
data class InternalConfig(
    var adClass: String = "o.\$r8\$lambda\$EDxcFcg09LKEZKxzlvZ-tMHN3ZU",
    var cookieClass: String = "com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor",
    var prefMonaKeyFile: String = "2chapi",
    var prefMonaKeyName: String = "2chapi_monakey",
)