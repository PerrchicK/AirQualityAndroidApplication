package com.perrchick.airqualityapplication.communication

import android.os.Bundle
import com.perrchick.airqualityapplication.util.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException

class Communicator {
    companion object {
        private val TAG: String = Communicator::class.simpleName.toString()
        val _apiKey: String = Configurations.ApiKey()
        val currentConditionsApiUrl =
            "air-quality/v2/current-conditions?key=$_apiKey&features=breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,pollutants_concentrations,pollutants_aqi_information&metadata=true"
        val forecastConditionsApiUrl =
            "air-quality/v2/forecast/hourly?key=$_apiKey&features=breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,pollutants_concentrations,pollutants_aqi_information&metadata=true"

        @JvmStatic fun fetchBaqi(latitude: Double, longitude: Double, callback: (Int?) -> Unit) {
            val locationString = "lat=$latitude&lon=$longitude"

            val conditionsApiUrl = "${Configurations.BaseUrl}$currentConditionsApiUrl"

            val urlString = "$conditionsApiUrl&$locationString"
            val url = urlString.toHttpUrlOrNull()?.newBuilder()?.build() ?: run {
                callback(null)
                return
            }

            val request = Request.Builder().url(url).build()

            val okHttpClient = OkHttpClient()
            try {
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        AppLogger.error(TAG, e)
                        callback(null)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            AppLogger.error(TAG, response)
                            callback(null)
                        } else {
                            AppLogger.log(TAG, response)

                            try {
                                val jsonString = response.body?.string()
                                val jsonObject = JSONObject(jsonString)
                                (jsonObject["data"] as? JSONObject)?.let { data ->
                                    (data["indexes"] as? JSONObject)?.let { indexes ->
                                        (indexes["baqi"] as? JSONObject)?.let { baqi ->
                                            val baqiLevel: Int? = (baqi["aqi"] as? Int)
                                            callback(baqiLevel)
                                        }
                                    }
                                }
                            } catch (parsingException: Throwable) {
                                AppLogger.error(TAG, "parsingException", parsingException)
                                callback(null)
                            }
                        }
                    }
                })
            } catch (t: Throwable) {
                Utils.debugToast("Failed to fetch BAQI due to: ${t.message}")
                AppLogger.error(TAG, t)
                callback(null)
            }
        }


        fun fetchPollutantValue(pollutantName: String, latitude: Double, longitude: Double, callback: (Double?) -> Unit) {
            val locationString = "lat=$latitude&lon=$longitude"

            val conditionsApiUrl = "${Configurations.BaseUrl}$currentConditionsApiUrl"

            val urlString = "$conditionsApiUrl&$locationString"
            val url = urlString.toHttpUrlOrNull()?.newBuilder()?.build() ?: run {
                callback(null)
                return
            }

            val request = Request.Builder().url(url).build()

            val okHttpClient = OkHttpClient()
            try {
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        AppLogger.error(TAG, e)
                        callback(null)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            AppLogger.error(TAG, response)
                            callback(null)
                        } else {
                            AppLogger.log(TAG, response)

                            try {
                                val jsonString = response.body?.string()
                                val jsonObject = JSONObject(jsonString)
                                (jsonObject["data"] as? JSONObject)?.let { data ->
                                    val pollutantConcentration = data.optJSONObject("pollutants")?.optJSONObject(pollutantName)?.optJSONObject("concentration")?.optDouble("value")

                                    (data["indexes"] as? JSONObject)?.let { indexes ->
                                        (indexes["baqi"] as? JSONObject)?.let { baqi ->
                                            val baqiLevel: Int? = (baqi["aqi"] as? Int)
                                            baqiLevel?.let {
                                                val details = Bundle()
                                                details.putInt(Constants.Keys.Baqi, baqiLevel)
                                                PrivateEventBus.notify(PrivateEventBus.Action.NEW_BAQI_ARRIVED, details)
                                            }
                                        }
                                    }

                                    callback(pollutantConcentration)
                                }
                            } catch (parsingException: Throwable) {
                                AppLogger.error(TAG, "parsingException", parsingException)
                                callback(null)
                            }
                        }
                    }
                })
            } catch (t: Throwable) {
                Utils.debugToast("Failed to fetch BAQI due to: ${t.message}")
                AppLogger.error(TAG, t)
                callback(null)
            }
        }
    }
}
