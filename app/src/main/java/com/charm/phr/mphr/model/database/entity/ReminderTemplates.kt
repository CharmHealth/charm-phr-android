package com.charm.phr.mphr.model.database.entity

/**
 * Created by deepaj-6611 on 03/11/22.
 */

//@Entity
//data class ReminderTemplates(
//    @PrimaryKey(autoGenerate = true) var localTemplateId: Long = 0,
//    var templateId: String? = null,
//    var type: String? = null,
//    var content: String? = null,
//    var createdTime: String? = null,
//    var modifiedTime: String? = null,
//    var isDeleted: String? = null,
//    var addedBy: String? = null,
//)


//    <table name="PHRReminderTemplates">
//		<description>Templates for PHR reminders</description>
//		<columns>
//			<column name="TEMPLATE_ID">
//				<description>Unique identifier</description>
//				<data-type>BIGINT</data-type>
//				<uniquevalue-generation>
//					<generator-name>PHRReminderTemplates.TEMPLATE_ID</generator-name>
//				</uniquevalue-generation>
//			</column>
//			<column name="TYPE">
//				<description>Type of the Item like Prescriptions, Supplements etc</description>
//				<data-type>CHAR</data-type>
//				<max-size>50</max-size>
//				<nullable>false</nullable>
//			</column>
//			<column name="CONTENT">
//                 <description>Location of the file</description>
//                 <data-type>CHAR</data-type>
//                 <max-size>300</max-size>
//                 <nullable>false</nullable>
//            </column>
//			<column name="CREATED_TIME">
//				<description>Created time of the Template</description>
//				<data-type>BIGINT</data-type>
//				<nullable>false</nullable>
//			</column>
//            <column name="MODIFIED_TIME">
//                <description>Time of modification in ms</description>
//                <data-type>BIGINT</data-type>
//                <nullable>true</nullable>
//            </column>
//			<column name="IS_DELETED">
//				<description>Template active/inactive status</description>
//				<data-type>BOOLEAN</data-type>
//				<default-value>false</default-value>
//				<nullable>false</nullable>
//			</column>
//			<column name="ADDED_BY">
//                <description>Identifier of Patient</description>
//                <data-type>BIGINT</data-type>
//                <nullable>false</nullable>
//            </column>
//		</columns>
//		<primary-key name="PHRReminderTemplates_PK">
//			<primary-key-column>TEMPLATE_ID</primary-key-column>
//		</primary-key>
//		<foreign-keys>
//            <foreign-key name="PHRReminderTemplates_FK1" reference-table-name="PatientInfo" isbidirectional="false">
//                <fk-columns>
//                    <fk-column>
//                            <fk-local-column>ADDED_BY</fk-local-column>
//                            <fk-reference-column>PATIENT_ID</fk-reference-column>
//                    </fk-column>
//                </fk-columns>
//                <fk-constraints>ON-DELETE-CASCADE</fk-constraints>
//            </foreign-key>
//        </foreign-keys>
//	</table>