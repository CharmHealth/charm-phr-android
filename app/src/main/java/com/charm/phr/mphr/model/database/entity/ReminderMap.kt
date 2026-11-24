package com.charm.phr.mphr.model.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Created by deepaj-6611 on 03/11/22.
 */

@Entity(foreignKeys = [(ForeignKey(entity = Reminder::class, parentColumns = ["localReminderId"], childColumns = ["localReminderId"], onDelete = ForeignKey.CASCADE))])
data class ReminderMap(
    @PrimaryKey(autoGenerate = true) var localReminderMapId: Long = 0,
    var reminderMapId: String? = null,
    var reminderId: String? = null,
    var localReminderId: Long = 0,
    var moduleId: String? = null,
    var type: String? = null,
    var patientId: String? = null,
    var practiceId: String? = null,
)


//   <table name="PHRReminderMap">
//		<description>Mapping table for PHR reminders</description>
//		<columns>
//			<column name="REMINDER_MAP_ID">
//				<description>Unique identifier</description>
//				<data-type>BIGINT</data-type>
//				<uniquevalue-generation>
//					<generator-name>PHRReminderMap.REMINDER_MAP_ID</generator-name>
//				</uniquevalue-generation>
//			</column>
//			<column name="REMINDER_ID">
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="MODULE_ID">
//                <description>identifier of the Item like Prescriptions, Supplements etc</description>
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//			<column name="TYPE">
//				<description>Type of the Item like Prescriptions, Supplements etc</description>
//				<data-type>CHAR</data-type>
//				<max-size>50</max-size>
//				<nullable>false</nullable>
//			</column>
//			<column name="PATIENT_ID">
//                <description>Identifier of Patient</description>
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="PRACTICE_ID">
//                <description>Identifier of Practice</description>
//                <data-type>BIGINT</data-type>
//                <nullable>true</nullable>
//            </column>
//		</columns>
//		<primary-key name="PHRReminderMap_PK">
//			<primary-key-column>REMINDER_MAP_ID</primary-key-column>
//		</primary-key>
//		<foreign-keys>
//            <foreign-key name="PHRReminderMap_FK1" reference-table-name="PHRReminders" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>REMINDER_ID</fk-local-column>
//                            <fk-reference-column>REMINDER_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//            <foreign-key name="PHRReminderMap_FK2" reference-table-name="PatientInfo" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>PATIENT_ID</fk-local-column>
//                            <fk-reference-column>PATIENT_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//        </foreign-keys>
//	</table>