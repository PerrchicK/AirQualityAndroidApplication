package com.perrchick.airqualityapplication.util

import com.perrchick.airqualityapplication.BuildConfig

// test
class Constants {
    object Extra {

        class Keys {
            companion object {
                const val SHOULD_SKIP_SPLASH = "SHOULD_SKIP_SPLASH"
                const val URL_STRING = "URL_STRING"
            }
        }
    }
    object FlutterMethodChannel {
        class Keys {
            companion object {
                const val FAILURE_RESULT = "0"
                const val SUCCESS_RESULT = "1"
            }
        }
    }

    companion object {
        const val ONE_MINUTE_MILLISECONDS: Long = 1000 * 60
        const val ONE_HOUR_IN_MILLISECONDS: Long = ONE_MINUTE_MILLISECONDS * 60
    }
}

enum class Environment {
    PRODUCTION, DEVELOPMENT;

    override fun toString(): String {
        return when (this) {
            DEVELOPMENT -> DEVELOPMENT_STRING
            PRODUCTION -> PRODUCTION_STRING
        }
    }

    companion object {
        private const val DEV_FLAVOR_STRING: String = "development"
        private const val DEVELOPMENT_STRING: String = "development"
        private const val PRODUCTION_STRING: String = "production"

        fun currentEnvironment(): Environment {
            @Suppress("ConstantConditionIf", "LiftReturnOrAssignment")
            if (BuildConfig.FLAVOR == DEV_FLAVOR_STRING) {
                return DEVELOPMENT
            } else {
                return PRODUCTION
            }
        }
    }

}