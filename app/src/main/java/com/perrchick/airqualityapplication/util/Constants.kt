package com.perrchick.airqualityapplication.util

import com.perrchick.airqualityapplication.BuildConfig

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

class Constants {
    class Keys {
        companion object {
            const val Baqi = "baqi"
        }
    }

}