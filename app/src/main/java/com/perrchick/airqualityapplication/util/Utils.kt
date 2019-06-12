package com.perrchick.airqualityapplication.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.BuildConfig
import java.util.*

class Utils {
    companion object {
        val instance = Utils()
        val isProductionVersion = Environment.currentEnvironment() == Environment.PRODUCTION
        val isReleaseVersion = !BuildConfig.DEBUG
        fun isRunningOnSimulator(): Boolean {
            return instance.isRunningOnSimulator
        }

        fun isRunningUnderDevelopmentEnvironment(): Boolean {
            return Environment.currentEnvironment() == Environment.DEVELOPMENT
        }

        fun random(a: Int, b: Int): Int {
            if (a == b) return b

            val _min: Int = Math.min(a, b)
            val _max: Int = Math.max(a, b)
            val diff: Int = _max - _min

            return Random().nextInt(diff) + _min
        }

        fun debugToast(toastMessage: String) {
            if (isReleaseVersion) return

            AirQualityApplication.shared()?.runOnUiThread {
                Toast.makeText(AirQualityApplication.shared(), toastMessage, Toast.LENGTH_LONG).show()
            }
        }

        fun showDialog(dialogMessage: String) {
        }

    }

    val screenWidth: Int
    val screenHeight: Int

    // sdk_google_phone_x86-userdebug 6.0 MASTER 3393901 test-keys
    private val isRunningOnSimulator: Boolean = !isReleaseVersion && (Build.FINGERPRINT.contains("generic") || Build.DISPLAY.startsWith("sdk_google_phone_") || Build.FINGERPRINT.contains("google/sdk"))

    init {
        val wm = AirQualityApplication.shared()?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
    }
}