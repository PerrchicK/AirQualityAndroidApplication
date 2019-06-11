package com.perrchick.airqualityapplication.bl

import android.icu.text.StringSearch
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.communication.Communicator
import com.perrchick.airqualityapplication.util.AppLogger
import com.perrchick.airqualityapplication.util.Strings
import com.perrchick.airqualityapplication.util.Utils

class DecisionMaker {
    companion object {
        @JvmStatic
        fun makeDecisionAccordingToBaqi() {
            Communicator.fetchBaqi(32.43232, 34.32132) { baqi ->
                AppLogger.log(baqi)
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
    }
}