package space.aioilight.tsubonofuta.config

import kotlinx.serialization.Serializable

@Serializable
data class InternalConfig(
    var threadClass: String = "jp.syoboi.a2chMate.view.MyAdView",
    var inlineClass: String = "jp.syoboi.a2chMate.view.ad.InlineAdContainer",
    var adActivityClass: String = "jp.syoboi.a2chMate.activity.ResListActivity",
    var videoManagerClass: String = "jp.supership.vamp.VAMPManager",
    var videoRequestClass: String = "jp.supership.vamp.VAMPRequest",
    var videoOpenMethod: String = "onOpened",
    var videoCompleteMethod: String = "onCompleted",
    var cookieClass: String = "com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor",
    var prefMonaKeyFile: String = "2chapi",
    var prefMonaKeyName: String = "2chapi_monakey",
)