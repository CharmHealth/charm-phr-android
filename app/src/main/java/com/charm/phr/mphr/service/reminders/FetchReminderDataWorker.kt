package com.charm.phr.mphr.service.reminders

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import java.util.concurrent.TimeUnit

class FetchReminderDataWorker(
    ctx: Context,
    params: WorkerParameters,
    val authManager: AuthManager,
    val reminderRepository: ReminderRepository,
    val prescriptionRepository: PrescriptionRepository,
    val miscRepository: MiscRepository
) :
    Worker(ctx, params) {

    companion object {
        fun fetchReminderDataRequest(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                FetchReminderDataWorker::class.java,
                ModelUtil.reminderFlexTimeRange.toLong(), TimeUnit.HOURS
            ).setConstraints(constraints).addTag(ModelUtil.getReminderDataTAG).build()

            WorkManager.getInstance(context).enqueue(periodicWorkRequest)
        }
    }

    private var medicationListTemp: ArrayList<PatMedPOJO> = arrayListOf()
    private var supplementListTemp: ArrayList<PatientSupplements> = arrayListOf()

    override fun doWork(): Result {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val userLoggedIn =
            pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)
        if (userLoggedIn) {
            val patientList = authManager.patientList
            patientList.forEach { patient ->
                patient.sectionConfigList =
                    authManager.getSelectedPatientSection(patient.practicePatientId ?: "")
                if (patient.sectionConfigList == null) {
                    patient.sectionConfigList = getPatientSection(patient)
                }
            }
            getMedicationsForAllPatient(patientList)
            getSupplementsForAllPatient(patientList)
            getReminderForAllPatient(patientList)
        }
        return Result.success()
    }

    private fun getReminder(patient: Patient) {
        //Get All Reminders From Server
        val getReminderResp = reminderRepository.getReminderSynchronize(
            null,
            null,
            null,
            patient.practicePatientId ?: ""
        )
        if (getReminderResp is ArrayList<*>) {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            pref.edit().putBoolean(ModelUtil.isReminderDataFetched, true).apply()
            val list = getReminderResp as ArrayList<ReminderPOJO>
            //Update the existing reminder from server and add new reminders
            reminderRepository.addOrUpdateReminderInLocalDb(list)
        }
    }


    private fun getReminderForAllPatient(patientList: ArrayList<Patient>) {
        patientList.forEach { patient ->
            getReminder(patient)
        }
    }


    private fun getMedicationsForAllPatient(patientList: ArrayList<Patient>) {
        patientList.forEach { patient ->
            fetchMedicationList(patient, 1)
        }
        reminderRepository.addOrUpdateMedicationsInLocalDb(
            MDParsingHelper.formMedicationListForDB(
                medicationListTemp,
                this.applicationContext
            )
        )
    }

    private fun getSupplementsForAllPatient(patientList: ArrayList<Patient>) {
        patientList.forEach { patient ->
            fetchSupplements(patient, 1)
        }
        reminderRepository.addOrUpdateSupplementsInLocalDb(
            MDParsingHelper.formSupplementListForDB(
                supplementListTemp
            )
        )
    }


    private fun fetchMedicationList(patient: Patient, medicationPageNo: Int) {
        val medicationResp =
            prescriptionRepository.getMedicationsSynchronize(
                ModelUtil.medicationViewTypeAll,
                medicationPageNo,
                false,
                patient,
                true
            )
        if (medicationResp is Pair<*, *>) {
            val medicationList = medicationResp.first as ArrayList<PatMedPOJO>
            medicationListTemp.addAll(medicationList)
            if (medicationResp.second as Boolean) {
                fetchMedicationList(patient, medicationPageNo + 1)
            }
        }
    }


    private fun fetchSupplements(patient: Patient, supplementPageNo: Int) {
        val supplementListRes =
            prescriptionRepository.getSupplementsSynchronize(
                ModelUtil.medicationViewTypeAll,
                supplementPageNo,
                patient,
                true
            )
        if (supplementListRes is Pair<*, *>) {
            val supplementList = supplementListRes.first as ArrayList<PatientSupplements>
            supplementListTemp.addAll(supplementList)
            if (supplementListRes.second as Boolean) {
                fetchSupplements(patient, supplementPageNo + 1)
            }
        }

    }

    private fun getPatientSection(patient: Patient): SectionConfigList? {
        val patientSectionResp = miscRepository.getPatientSectionSynchronize(patient)
        if (patientSectionResp is SectionConfigList) {
            return patientSectionResp
        }
        return null
    }
}