package com.shaun.stocktracker.data.converter

import androidx.room.TypeConverter
import com.shaun.stocktracker.data.entity.AlertType

class Converters {
    @TypeConverter
    fun fromAlertType(alertType: AlertType): String = alertType.name

    @TypeConverter
    fun toAlertType(value: String): AlertType = AlertType.fromString(value)
}
