package com.perrchick.airqualityapplication.util

import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.perrchick.airqualityapplication.AirQualityApplication

class Strings {
    companion object {
        fun localized(@StringRes resourceId: Int): String? {
            return AirQualityApplication.shared()?.getString(resourceId)
        }

        fun localized(stringId: String): String {
            return stringId //BrzApplication.shared().getString(stringId)
        }

        val POOR: String =
            "It is recommended to close the windows, turn on the air purifier, and embrace an indoor chill out"
        val LOW: String = "Another stay indoors day, unfortunately."
        val MODERATE: String = "This is not a day for a marathon but walking the dog should probably be ok."
        val GOOD: String =
            "Feel free to work off yesterday's extra order of fries in the outdoors, but take it indoors if you start to feel wheezy."
        val EXCELLENT: String =
            "Nothing can stop you now, you child of nature, breathe that fresh air in, the outdoors is your playground."

        val BAQI_DESCRIPTIONS = arrayOf("Poor", "Low", "Moderate", "Good", "Excellent")
    }
}
