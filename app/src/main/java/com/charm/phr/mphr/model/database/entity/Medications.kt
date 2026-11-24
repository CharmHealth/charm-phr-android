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
data class Medications(
    @PrimaryKey(autoGenerate = true) var localMedicationId: Long = 0,
    var medicationId: String? = null,
    var directions: String? = null,
    var genericDrugName: String? = null,
    var isDeleted: Boolean = false,
    var startDate: DateTime? = null,
    var endDate: DateTime? = null,
    var practicePatientId: String? = null,
    var practiceId: String? = null,
): Parcelable

/*
<table name="PatientMedications">
<description>Medications used by patients (For Practice Purpose)</description>
<columns>
<column name="PATIENT_MEDICATION_ID">
<description>Unique identifier</description>
<data-type>BIGINT</data-type>
<uniquevalue-generation>
<generator-name>PatientMedications.PATIENT_MEDICATION_ID</generator-name>
</uniquevalue-generation>
</column>
<column name="DATE_OF_ENTRY">
<description>Date and Time on which this data is entered</description>
<data-type>DATE</data-type>
<nullable>false</nullable>
</column>
<column name="DRUGDETAILS_ID">
<description>Identifier of Drug Id</description>
<data-type>BIGINT</data-type>
<nullable>false</nullable>
</column>
<column name="PRACTICE_PATIENT_ID">
<description>Identifier of Patient</description>
<data-type>BIGINT</data-type>
<nullable>false</nullable>
</column>
<column name="START_DATE">
<description>Date from which the prescription is taken</description>
<data-type>DATE</data-type>
<nullable>true</nullable>
</column>

<column name="STOP_DATE">
<description>Date till which the prescription is taken</description>
<data-type>DATE</data-type>
<nullable>true</nullable>
</column>
<column name="DOSAGE">
<description>Dosage</description>
<data-type>FLOAT</data-type>
<nullable>true</nullable>
</column>

<column name="DOSAGE_UNIT_ID">
<description>Dosage Unit</description>
<data-type>BIGINT</data-type>
<nullable>true</nullable>
</column>
<column name="INTAKE">
<description>Daily Intake</description>
<data-type>CHAR</data-type>
<nullable>false</nullable>
</column>
<column name="INTAKE_ROUTE">
<description>Intake Route</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
<nullable>false</nullable>
</column>
<column name="IS_ACTIVE">
<description> Whether the Medication is currently active or not </description>
<data-type>BOOLEAN</data-type>
<nullable>false</nullable>
</column>
<column name="INTAKE_TYPE">
<description>Before meals/After meals</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
</column>
<column name="DOSAGE_MORNING">
<description>Dosage taken in the morning</description>
<data-type>FLOAT</data-type>
</column>
<column name="DOSAGE_AFTERNOON">
<description>Dosage taken in the afternoon</description>
<data-type>FLOAT</data-type>
</column>
<column name="DOSAGE_EVENING">
<description>Dosage taken in the evening</description>
<data-type>FLOAT</data-type>
</column>
<column name="DOSAGE_NIGHT">
<description>Dosage taken in the  night</description>
<data-type>FLOAT</data-type>
</column>
<column name="INTAKE_TYPE_MORNING">
<description>Intake type in the morning</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
</column>
<column name="INTAKE_TYPE_AFTERNOON">
<description>Intake type in the afternoon</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
</column>
<column name="INTAKE_TYPE_EVENING">
<description>Intake type in the evening</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
</column>
<column name="INTAKE_TYPE_NIGHT">
<description>Intake type in the night</description>
<data-type>CHAR</data-type>
<max-size>50</max-size>
</column>

<column name="IS_DIFFERENT_DOSAGE">
<data-type>BOOLEAN</data-type>
<default-value>false</default-value>
<nullable>false</nullable>
</column>
<column name="COMMENT">
<description>Medication Comment</description>
<data-type>CHAR</data-type>
<max-size>250</max-size>
</column>

</columns>
<primary-key name="PatientMedications_PK">
<primary-key-column>PATIENT_MEDICATION_ID</primary-key-column>
</primary-key>
<foreign-keys>
<foreign-key name="PatientMedications_FK" reference-table-name="PracticePatientsList" isbidirectional="false">
<fk-columns>
<fk-column>
<fk-local-column>PRACTICE_PATIENT_ID</fk-local-column>
<fk-reference-column>PRACTICE_PATIENT_ID</fk-reference-column>
</fk-column>
</fk-columns>
<fk-constraints>ON-DELETE-CASCADE</fk-constraints>
</foreign-key>
</foreign-keys>

<indexes>
<index name="PatientMedications_IDX2">
<index-column>DATE_OF_ENTRY</index-column>
</index>
</indexes>
</table>*/
