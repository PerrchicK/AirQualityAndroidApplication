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
        val instance = Utils() // rename to: AdvertisementsManager
        val isProductionVersion = Environment.currentEnvironment() == Environment.PRODUCTION
        val isReleaseVersion = !BuildConfig.DEBUG
        fun isRunningOnSimulator(): Boolean {
            return instance.isRunningOnSimulator
        }

        fun isRunningUnderDevelopmentEnvironment(): Boolean {
            return Environment.currentEnvironment() == Environment.DEVELOPMENT
        }

        fun now(shouldConsiderDiff: Boolean = true): Long {
            return instance.now(shouldConsiderDiff)
        }

        fun random(a: Int, b: Int): Int {
            if (a == b) return b

            val _min: Int = Math.min(a, b)
            val _max: Int = Math.max(a, b)
            val diff: Int = _max - _min

            return Random().nextInt(diff) + _min
        }

        fun getCurrentTimestamp(): Long {
            return now()
        }

        fun debugToast(toastMessage: String) {
            if (isReleaseVersion) return

            Toast.makeText(AirQualityApplication.shared(), toastMessage, Toast.LENGTH_LONG).show()
        }

        fun baqiLocalizedDescription(baqi: Int): String? {
            return categoryLevelIndex(baqi)?.let { index ->
                Strings.BAQI_DESCRIPTIONS.safeIndex(index)?.localized()
            } ?: run {
                null
            }
        }

        fun categoryLevelIndex(baqi: Int): Int? {
            if (baqi < 0 || baqi > 100) {
                return null
            }

            val index: Int?
            when (baqi) {
                in 80..100 -> {
                    index = 4
                }
                in 60..80 -> {
                    index = 3
                }
                in 40..60 -> {
                    index = 2
                }
                in 20..40 -> {
                    index = 1
                }
                in 0..20 -> {
                    index = 0
                }
                else -> {
                    index = null
                }
            }

            return index
        }

        fun showDialog(dialogMessage: String) {
        }

    }

    private var timeDiff = 0

    fun setTimeDiff(diff: Int) {
        timeDiff = diff
    }

    private fun now(shouldConsiderDiff: Boolean = true): Long {
        val _timeDiff = when (shouldConsiderDiff) {
            true -> {
                timeDiff
            }
            else -> {
                0
            }
        }

        return System.currentTimeMillis() + _timeDiff
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

private fun <T> Array<T>.safeIndex(index: Int?): T? {
    if (index == null) return null
    if (index < 0) return null
    if (index >= size) return null

    return this[index]
}

fun String.localized(): String {
    return Strings.localized(this)
}
