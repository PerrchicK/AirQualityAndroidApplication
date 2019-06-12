package com.perrchick.airqualityapplication.util

import com.perrchick.airqualityapplication.R

class Configurations {
    companion object {
        private var _breezoApiKey: String = ""

        const val BaseUrl: String = "https://api.breezometer.com/"

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
    }
}
