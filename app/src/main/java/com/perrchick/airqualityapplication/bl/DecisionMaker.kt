package com.perrchick.airqualityapplication.bl

import android.app.Activity
import com.google.android.gms.maps.model.LatLng
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.communication.Communicator
import com.perrchick.airqualityapplication.dl.DrugScore
import com.perrchick.airqualityapplication.util.AppLogger
import com.perrchick.airqualityapplication.util.Strings
import com.perrchick.airqualityapplication.util.Utils
import java.lang.ref.WeakReference

class DecisionMaker {
    companion object {
        private val TAG: String = DecisionMaker::class.simpleName.toString()

        @JvmStatic
        fun makeDecisionAccordingToBaqi() {
            Communicator.fetchBaqi(32.8191541, 34.9984632) { baqi ->
                AppLogger.log(TAG, baqi)
                //Explanation about Kotlin's `when`: https://kotlinlang.org/docs/reference/control-flow.html
                when (baqi) {
                    in 0..20 -> {
                        Utils.showDialog(Strings.POOR)
                    }
                    in 21..40 -> {
                        Utils.showDialog(Strings.LOW)
                    }
                    in 41..60 -> {
                        Utils.showDialog(Strings.MODERATE)
                    }
                    in 61..80 -> {
                        Utils.showDialog(Strings.GOOD)
                    }
                    in 81..100 -> {
                        Utils.showDialog(Strings.EXCELLENT)
                    }
                    else -> {
                        print("error: this BAQI is unknown 😯")
                    }
                }
            }

        }

        private fun fetchPollutantValue(
            pollutantName: String,
            latitude: Double,
            longitude: Double,
            callback: (Double?) -> Unit
        ) {
            Communicator.fetchPollutantValue(pollutantName.toLowerCase(), latitude, longitude, callback)
        }

        fun onUserChoseLocation(clickedLocation: LatLng, listener: DrugScoreListener) {

            /* Use this to avoid memory leaks, or use LiveData: https://developer.android.com/topic/libraries/architecture/livedata */
            var weakActivity: WeakReference<Activity>? = null
            (listener as? Activity)?.let { activity ->
                weakActivity = WeakReference(activity)
            }
            val weakListener = WeakReference(listener)
            /* Use this to avoid memory leaks (meaning, avoid holding an activity's reference when it's not necessary), or use LiveData */

            val pollutantName =
                "pm25" // Now it's PM2.5, tomorrow it could be "CO", after tomorrow it could be Crystal Meth particles :)
            fetchPollutantValue(pollutantName, clickedLocation.latitude, clickedLocation.longitude) { value ->
                AppLogger.log(value)

                var drugScore: DrugScore? = null
                value?.let {
                    drugScore = DrugScore(it, clickedLocation)
                    //TODO: Cache result / save in DB for future use or research later...
                }

                weakActivity?.get()?.isDestroyed?.let { isDestroyed ->
                    if (isDestroyed) {
                        // No need to update the listener (because it's an already destroyed activity)
                        return@fetchPollutantValue
                    }
                }

                /* We are now in a network thread, we are not allowed to touch UI now... */

                AirQualityApplication.shared()?.runOnUiThread {
                    /* We are now in the UI thread, now we are allowed to touch UI */

                    Utils.debugToast("'$pollutantName' concentration value: $value")

                    // This `listenerOrNull` might be null because it might no longer exists.
                    // It mostly depends on the garbage collector's work and on the need of this object in memory.
                    val listenerOrNull = weakListener.get()

                    listenerOrNull?.onUpdate(drugScore)
                }
            }
        }
    }

    interface DrugScoreListener {
        fun onUpdate(drugScore: DrugScore?)
    }
}