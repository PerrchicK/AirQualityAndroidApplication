package com.perrchick.airqualityapplication.util

import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.R

class Configurations {
    companion object {
        // TODO Change it dynamically according to.... ... .. something that cloud determine if we are actually running under the Chinese great-firewall
        // Should solve this 'https://en.wikipedia.org/wiki/Great_Firewall' by simply using our AWS endpoint
        var shouldUseChineseUrl: Boolean = false
        private var _breezoApiKey: String = ""

        const val NonChineseBaseUrl: String = "https://api.breezometer.com/"
        const val ChineseBaseUrl: String = "http://aqi-info.com/"

        fun ApiKey(): String {
            if (_breezoApiKey.isNotEmpty()) return _breezoApiKey

            var breezoApiKey: String?
            if (!Utils.isReleaseVersion || !Utils.isProductionVersion) {
                breezoApiKey = Strings.localized(R.string.production_breezo_key)
            } else {
                breezoApiKey = Strings.localized(R.string.development_breezo_key)
            }

            if (breezoApiKey == null) {
                run {
                    AppLogger.error(this, "Missing BreezoMeter key!")
                    Utils.debugToast("Missing BreezoMeter key!")
                }
            } else {
                _breezoApiKey = breezoApiKey
            }

            return _breezoApiKey
        }

        const val MINIMUM_DISTANCE_WITHOUT_ALERT_IN_METERS: Int = 1000
    }
}
