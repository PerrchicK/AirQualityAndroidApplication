package com.perrchick.airqualityapplication.dl

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng

class DrugScore(private val value: Double, val location: LatLng) {
    fun color(): Float { // Or: return `android.graphics.Color`
        when (value) {
            in 0..20 -> {
                return BitmapDescriptorFactory.HUE_GREEN
            }
            in 21..40 -> {
                return BitmapDescriptorFactory.HUE_BLUE
            }
            in 41..60 -> {
                return BitmapDescriptorFactory.HUE_RED
            }
            in 61..80 -> {
                return BitmapDescriptorFactory.HUE_ROSE
            }
            else -> {
                return BitmapDescriptorFactory.HUE_AZURE
            }
        }
    }
}
