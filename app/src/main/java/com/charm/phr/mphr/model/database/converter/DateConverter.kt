package com.charm.phr.mphr.model.database.converter

import androidx.room.TypeConverter
import org.joda.time.DateTime
import org.joda.time.LocalDate

/**
 * Created by deepaj-6611 on 11/11/22.
 */
class DateConverter {

    @TypeConverter
    fun timestampToDateTime(value: Long?): DateTime? {
        return if (value == null) null else DateTime(value)
    }

    @TypeConverter
    fun dateTimeToTimestamp(date: DateTime?): Long? {
        return date?.millis
    }

    @TypeConverter
    fun timestampToLocalDate(value: Long?): LocalDate? {
        return if (value == null) null else LocalDate(value)
    }

    @TypeConverter
    fun localDateToTimestamp(localDate: LocalDate?): Long? {
        return localDate?.toDateTimeAtStartOfDay()?.millis
    }

}