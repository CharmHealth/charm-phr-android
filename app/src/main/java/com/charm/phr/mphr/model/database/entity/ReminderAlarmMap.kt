package com.charm.phr.mphr.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.charm.phr.mphr.model.database.converter.DateConverter
import org.joda.time.DateTime

@Entity
@TypeConverters(
    DateConverter::class)
data class FrequencyAlarmMap(
    @PrimaryKey(autoGenerate = true) var localFrequencyAlarmMapId: Long = 0,
    var localFrequencyMapId: Long = 0,
    var scheduledTime: DateTime? = null,
    var patientId:String?=null,
)
