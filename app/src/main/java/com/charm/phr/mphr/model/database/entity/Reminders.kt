package com.charm.phr.mphr.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.charm.phr.mphr.model.database.converter.DateConverter
import com.charm.phr.mphr.model.database.converter.SyncStatusConverter
import org.joda.time.DateTime

/**
 * Created by deepaj-6611 on 03/11/22.
 */

enum class SyncStatus {
    NOT_SYNCED, SYNCING, SYNC_SUCCESS, SYNC_FAILURE
}

enum class SyncOperation {
    ADD, UPDATE, DELETE
}


@Entity
@TypeConverters(DateConverter::class, SyncStatusConverter::class)
data class Reminder(
    @PrimaryKey(autoGenerate = true) var localReminderId: Long = 0,
    var reminderId: String? = null,
    var createdTime: DateTime? = null,
    var modifiedTime: DateTime? = null,
    var startDate: DateTime? = null,
    var frequency: String? = null,
    var frequencyUnit: String? = null,  // minute/hour/day/week/month/year
    var endDate: DateTime? = null,
    var isDeleted: Boolean = false,
    var notificationType: String? = null, //mobile-app
    var addedBy: String? = null,
    var templateId: String? = null,
    var localStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    var reminderStatus: String? = null
)


//   <table name="PHRReminders">
//    <description>Table to store the data of PHR reminders.</description>
//        <columns>
//            <column name="REMINDER_ID">
//                <data-type>BIGINT</data-type>
//                <uniquevalue-generation>
//                    <generator-name>PHRReminders.REMINDER_ID</generator-name>
//                </uniquevalue-generation>
//            </column>
//            <column name="CREATED_TIME">
//                <description>Time of addition in ms</description>
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="MODIFIED_TIME">
//                <description>Time of modification in ms</description>
//                <data-type>BIGINT</data-type>
//                <nullable>true</nullable>
//            </column>
//            <column name="START_DATE">
//				<description>Start date of the reminder</description>
//				<data-type>DATE</data-type>
//				<nullable>false</nullable>
//			</column>
//			 <column name="FREQUENCY">
//                 <description>Reminder frequency</description>
//                 <data-type>INTEGER</data-type>
//                 <default-value>0</default-value>
//                 <nullable>false</nullable>
//            </column>
//            <column name="FREQUENCY_UNIT">
//				<data-type>CHAR</data-type>
//				<max-size>10</max-size>
//				<nullable>false</nullable>
//				<allowed-values>
//                    <value>minute</value>
//					<value>hour</value>
//					<value>day</value>
//					<value>week</value>
//					<value>month</value>
//					<value>year</value>
//                </allowed-values>
//			</column>
//			<column name="END_DATE">
//				<description>End date of the reminder</description>
//				<data-type>DATE</data-type>
//				<nullable>true</nullable>
//			</column>
//			<column name="IS_DELETED">
//              <description>reminder active/inactive status</description>
//              <data-type>BOOLEAN</data-type>
//              <default-value>false</default-value>
//              <nullable>false</nullable>
//            </column>
//            <column name="NOTIFICATION_TYPE">
//				<data-type>CHAR</data-type>
//				<max-size>10</max-size>
//				<nullable>false</nullable>
//				<allowed-values>
//                    <value>mobile-app</value>
//                </allowed-values>
//			</column>
//			<column name="ADDED_BY">
//                <description>Identifier of Patient</description>
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="TEMPLATE_ID">
//              <description>Mapping from Template table</description>
//              <data-type>BIGINT</data-type>
//              <nullable>true</nullable>
//          </column>
//        </columns>
//        <primary-key name="PHRReminders_PK">
//            <primary-key-column>REMINDER_ID</primary-key-column>
//        </primary-key>
//        <foreign-keys>
//            <foreign-key name="PHRReminders_FK1" reference-table-name="PatientInfo" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>ADDED_BY</fk-local-column>
//                            <fk-reference-column>PATIENT_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//        </foreign-keys>
//    </table>