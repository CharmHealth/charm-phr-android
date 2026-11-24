package com.charm.phr.mphr.model.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.charm.phr.mphr.model.database.converter.DateConverter
import org.joda.time.DateTime

/**
 * Created by deepaj-6611 on 03/11/22.
 */

@Entity(foreignKeys = [ForeignKey(entity = ReminderFrequencyMap::class,
    parentColumns = ["localFrequencyMapId"],
    childColumns = ["localFrequencyMapId"],
    onDelete = ForeignKey.CASCADE), ForeignKey(entity = ReminderMap::class,
    parentColumns = ["localReminderMapId"],
    childColumns = ["localReminderMapId"],
    onDelete = ForeignKey.CASCADE)])

@TypeConverters(DateConverter::class)
data class ReminderHistory(
    @PrimaryKey(autoGenerate = true) var localHistoryId: Long = 0,
    var historyId: String? = null,
    var reminderMapId: String? = null,
    var localReminderMapId: Long = 0,
    var frequencyMapId: String? = null,
    var localFrequencyMapId: Long = 0,
    var status: Int? = null,
    var statusUpdatedTime: DateTime? = null,
    var isDeleted: Boolean = false,
    var reminderTime: String? = null,
)


//   <table name="PHRReminderHistory">
//		<description>History for PHR reminders</description>
//		<columns>
//			<column name="HISTORY_ID">
//				<description>Unique identifier</description>
//				<data-type>BIGINT</data-type>
//				<uniquevalue-generation>
//					<generator-name>PHRReminderHistory.HISTORY_ID</generator-name>
//				</uniquevalue-generation>
//			</column>
//			<column name="REMINDER_MAP_ID">
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="FREQUENCY_MAP_ID">
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//            <column name="STATUS">
//                <description>Intake status: 0-skipped, 1-taken</description>
//                <data-type>INTEGER</data-type>
//                <nullable>false</nullable>
//                <allowed-values>
//                  <value>0</value>
//                  <value>1</value>
//                </allowed-values>
//            </column>
//            <column name="STATUS_UPDATED_TIME">
//				<description>Time of status updated by the patient</description>
//				<data-type>DATETIME</data-type>
//				<nullable>false</nullable>
//			</column>
//			<column name="IS_DELETED">
//              <description>History deleted status</description>
//              <data-type>BOOLEAN</data-type>
//              <default-value>false</default-value>
//              <nullable>false</nullable>
//            </column>
//		</columns>
//		<primary-key name="PHRReminderHistory_PK">
//			<primary-key-column>HISTORY_ID</primary-key-column>
//		</primary-key>
//		<foreign-keys>
//            <foreign-key name="PHRReminderHistory_FK1" reference-table-name="PHRReminderMap" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>REMINDER_MAP_ID</fk-local-column>
//                            <fk-reference-column>REMINDER_MAP_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//            <foreign-key name="PHRReminderHistory_FK2" reference-table-name="PHRReminderFrequencyMap" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>FREQUENCY_MAP_ID</fk-local-column>
//                            <fk-reference-column>FREQUENCY_MAP_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//        </foreign-keys>
//	</table>