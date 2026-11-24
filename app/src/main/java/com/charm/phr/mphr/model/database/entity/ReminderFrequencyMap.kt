package com.charm.phr.mphr.model.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.charm.phr.mphr.model.database.converter.DateConverter

/**
 * Created by deepaj-6611 on 03/11/22.
 */

@Entity(foreignKeys = [(ForeignKey(entity = Reminder::class, parentColumns = ["localReminderId"], childColumns = ["localReminderId"], onDelete = ForeignKey.SET_NULL))])
@TypeConverters(DateConverter::class)
data class ReminderFrequencyMap(
    @PrimaryKey(autoGenerate = true) var localFrequencyMapId: Long = 0,
    var frequencyMapId: String? = null,
    var reminderId: String? = null,
    var localReminderId: Long? = null,
    var day: Int = -1, //0-sunday and 6-saturday // -1 not configured default value
    var time: String? = null,
    var isDeleted: Boolean = false,
    var notificationId: String? = null,
)


//   <table name="PHRReminderFrequencyMap">
//		<description>Frequency mapping for PHR reminders</description>
//		<columns>
//			<column name="FREQUENCY_MAP_ID">
//				<description>Unique identifier</description>
//				<data-type>BIGINT</data-type>
//				<uniquevalue-generation>
//                <generator-name>PHRReminderFrequencyMap.FREQUENCY_MAP_ID</generator-name>
//				</uniquevalue-generation>
//			</column>
//			<column name="REMINDER_ID">
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="DAY">
//              <description>Integer format representing day. 0-sunday and 6-saturday</description>
//              <data-type>INTEGER</data-type>
//              <nullable>true</nullable>
//            </column>
//			<column name="TIME">
//              <description>String denoting time like 1:00 in 24 hr format</description>
//              <data-type>CHAR</data-type>
//              <max-size>5</max-size>
//              <nullable>false</nullable>
//            </column>
//            <column name="IS_DELETED">
//              <description>Reminder time active/inactive status</description>
//              <data-type>BOOLEAN</data-type>
//              <default-value>false</default-value>
//              <nullable>false</nullable>
//            </column>
//		</columns>
//		<primary-key name="PHRReminderFrequencyMap_PK">
//			<primary-key-column>FREQUENCY_MAP_ID</primary-key-column>
//		</primary-key>
//		<foreign-keys>
//            <foreign-key name="PHRReminderFrequencyMap_FK1" reference-table-name="PHRReminders" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>REMINDER_ID</fk-local-column>
//                            <fk-reference-column>REMINDER_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//        </foreign-keys>
//	</table>