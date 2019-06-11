package com.perrchick.airqualityapplication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.BuildConfig

class PrivateEventBus {
    object Action {
        const val APPLICATION_GOING_BACKGROUND = BuildConfig.APPLICATION_ID + " - yo"
        const val APPLICATION_GOING_FOREGROUND = BuildConfig.APPLICATION_ID + " - yoyo"
        const val FIREBASE_IS_READY = BuildConfig.APPLICATION_ID + " - runs whenever firebase is connected and has an updated clock diff"
        const val NEW_BAQI_ARRIVED: String = BuildConfig.APPLICATION_ID + " - new_baqi_is_here"
        const val UPDATE_LOCATION: String = BuildConfig.APPLICATION_ID + " - location updated"
        const val MAP_TAP: String = BuildConfig.APPLICATION_ID + " - map_tap"
    }

    object Parameter {
        const val COORDINATES = "coordinates"
    }
    interface BroadcastReceiverListener {
        fun onBroadcastReceived(intent: Intent, receiver: PrivateEventBus.Receiver)
    }


    class Receiver

    /**
     * The receiver will live as long as the context lives. Therefore we will pass the application context in most of the times.
     * @param actionsToListen
     */
    constructor(private val actions: Collection<String>) : BroadcastReceiver() {
        private var receiverListener: BroadcastReceiverListener? = null

        init {
            if (actions.isNotEmpty()) {
                val intentFilter = IntentFilter()
                for (actionToListen in actions) {
                    intentFilter.addAction(actionToListen)
                }
                AirQualityApplication.shared()?.let {
                    LocalBroadcastManager.getInstance(it).registerReceiver(this, intentFilter)
                }
            }
        }

        fun setListener(listener: BroadcastReceiverListener) {
            this.receiverListener = listener
        }

        override fun onReceive(context: Context, intent: Intent) {
            receivedAction(intent)
        }

        private fun receivedAction(intent: Intent?) {
            if (receiverListener == null) {
                AppLogger.error(TAG, "onBroadcastReceived: Missing listener! Intent == " + intent!!)
            } else if (intent != null && !TextUtils.isEmpty(intent.action)) {
                receiverListener!!.onBroadcastReceived(intent, this)
            }
        }

        override fun toString(): String {
            return actions.toString()
        }

        fun quit() {
            try {
                AirQualityApplication.shared()?.let {
                    LocalBroadcastManager.getInstance(it).unregisterReceiver(this)
                }
                AppLogger.log(TAG, "removeReceiver: quit successfully: $this")
            } catch (e: Exception) {
                AppLogger.error(TAG, "removeReceiver: couldn't quitReceiving receiver: " + e.message)
            }

            receiverListener = null
        }
    }

    companion object {
        private val TAG: String = PrivateEventBus::javaClass.name

        fun createNewReceiver(listener: BroadcastReceiverListener, vararg actions: String): PrivateEventBus.Receiver {
            val receiver = Receiver(actions.asList())
            receiver.setListener(listener)
            return receiver
        }

        @JvmOverloads
        fun notify(action: String, extraValues: Bundle? = null) {
            val broadcastIntent = Intent(AirQualityApplication.instance(), PrivateEventBus::class.java).setAction(action)
            if (extraValues != null && extraValues.size() > 0) {
                broadcastIntent.putExtras(extraValues)
            }

            AirQualityApplication.getInstance()?.let {
                LocalBroadcastManager.getInstance(it).sendBroadcast(broadcastIntent)
            }
        }
    }
}