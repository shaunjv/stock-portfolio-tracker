package com.shaun.stocktracker.data.entity

/**
 * Extensible alert type enum.
 * Future types: PERCENTAGE_GAIN, PERCENTAGE_LOSS, TRAILING_STOP,
 * RSI_ABOVE, RSI_BELOW, MA_CROSSOVER, VOLUME_SPIKE
 */
enum class AlertType(val displayName: String) {
    ABOVE("Price Above"),
    BELOW("Price Below");

    companion object {
        fun fromString(value: String): AlertType =
            entries.find { it.name == value } ?: ABOVE
    }
}
