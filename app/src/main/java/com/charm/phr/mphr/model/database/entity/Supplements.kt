package com.charm.phr.mphr.model.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.charm.phr.mphr.model.database.converter.DateConverter
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Entity
@TypeConverters(
    DateConverter::class
)
@Parcelize
data class Supplements(
    @PrimaryKey(autoGenerate = true) var localSupplementId: Long = 0,
    var supplementId: String? = null,
    var directions: String? = null,
    var genericDrugName: String? = null,
    var isDeleted: Boolean = false,
    var fromDate: DateTime? = null,
    var toDate: DateTime? = null,
    var practicePatientId: String? = null,
    var practiceId: String? = null
): Parcelable