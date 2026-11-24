package com.charm.phr.mphr.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.JsonArray

class RbacUtil {
    companion object {
        enum class RBACPrivileges {

            //Wellness
            ADD_WELLNESS,
            VIEW_WELLNESS,
            EDIT_WELLNESS,
            DELETE_WELLNESS,

            //reports
            VIEW_REPORTS,

            //insurance
            ADD_INSURANCE,
            VIEW_INSURANCE,
            EDIT_INSURANCE,
            DELETE_INSURANCE,

            //immune
            ADD_IMMUNE,
            VIEW_IMMUNE,
            EDIT_IMMUNE,
            DELETE_IMMUNE,

            //procedure
            ADD_PROCEDURE,
            VIEW_PROCEDURE,
            EDIT_PROCEDURE,
            DELETE_PROCEDURE,

            //allergies
            ADD_ALLERGIES,
            VIEW_ALLERGIES,
            EDIT_ALLERGIES,
            DELETE_ALLERGIES,

            //labresults
            ADD_LABRESULTS,
            VIEW_LABRESULTS,
            EDIT_LABRESULTS,
            DELETE_LABRESULTS,

            //laborders
            VIEW_LABORDERS,

            //medications
            ADD_MEDICATIONS,
            VIEW_MEDICATIONS,
            EDIT_MEDICATIONS,
            DELETE_MEDICATIONS,

            //supplements
            ADD_SUPPLEMENTS,
            VIEW_SUPPLEMENTS,
            EDIT_SUPPLEMENTS,
            DELETE_SUPPLEMENTS,

            //invoice
            VIEW_INVOICE,
            VIEW_RECEIPT,
            ADD_PAYMENT,

            //visitsummary
            VIEW_VISITSUMMARY,

            //questionnaire
            VIEW_QUESTIONNAIRE,
            SUBMIT_QUESTIONNAIRE,

            //documents
            VIEW_DOCUMENTS,
            ADD_DOCUMENTS,
            DELETE_DOCUMENTS,
            EXPORT_DOCUMENTS,
            VIEW_FOLDERS,
            ADD_FOLDERS,
            EDIT_FOLDERS,
            DELETE_FOLDERS,

            //messages
            VIEW_MESSAGES,
            ADD_MESSAGES,
            DELETE_MESSAGES,

            //flowsheetdata
            ADD_FLOWSHEETDATA,
            EDIT_FLOWSHEETDATA,
            DELETE_FLOWSHEETDATA,
            VIEW_FLOWSHEETDATA,

            //appointment
            ADD_APPOINTMENT,
            VIEW_APPOINTMENT,
            DELETE_APPOINTMENT,
            CANCEL_APPOINTMENT,

            //clinicalsummary
            VIEW_CLINICALSUMMARY,
            EXPORT_CLINICALSUMMARY,

            //usefulinfo
            VIEW_USEFULINFO,

            //phrdata
            EXPORT_PHRDATA,

            //cardonfile
            VIEW_CARDONFILE,
            ADD_CARDONFILE,

            //section_config
            VIEW_SECTION_CONFIG,
            EDIT_SECTION_CONFIG,

            //familymembers
            VIEW_FAMILYMEMBERS,
            EDIT_FAMILYMEMBERS,

            //Not added Modules

            //healthchannel
            VIEW_HEALTHCHANNEL,

            //growthchart
            VIEW_GROWTHCHART,

            //library
            VIEW_LIBRARY,

            //therapy
            ADD_THERAPY,
            VIEW_THERAPY,
            EDIT_THERAPY,
            DELETE_THERAPY,

            //diet
            ADD_DIET,
            VIEW_DIET,
            EDIT_DIET,
            DELETE_DIET,

            //problem
            ADD_PROBLEM,
            VIEW_PROBLEM,
            EDIT_PROBLEM,
            DELETE_PROBLEM,

            //Images
            VIEW_IMAGERESULTS,
            VIEW_IMAGEORDERS,

            //timeline
            VIEW_TIMELINE,

            //announcements
            VIEW_ANNOUNCEMENTS,

            //Store
            VIEW_ONLINESTORE,

            //doctors
            ADD_DOCTORS,
            VIEW_DOCTORS,
            EDIT_DOCTORS,
            DELETE_DOCTORS,

            //pharmacy
            ADD_PHARMACY,
            VIEW_PHARMACY,
            EDIT_PHARMACY,
            DELETE_PHARMACY,

            //hospitals
            ADD_HOSPITALS,
            VIEW_HOSPITALS,
            EDIT_HOSPITALS,
            DELETE_HOSPITALS,

            //othercontact
            ADD_OTHERCONTACT,
            VIEW_OTHERCONTACT,
            EDIT_OTHERCONTACT,
            DELETE_OTHERCONTACT,

            //audits
            VIEW_AUDITS,

            //accountinfo
            VIEW_ACCOUNTINFO,
            EDIT_ACCOUNTINFO,

            //password
            EDIT_PASSWORD,

            //healthpassport
            VIEW_HEALTHPASSPORT,
            EDIT_HEALTHPASSPORT,
            EXPORT_HEALTHPASSPORT,

            //healthcard
            VIEW_HEALTHCARD,

            //other_associated_members
            VIEW_OTHER_ASSOCIATED_MEMBERS,

            //practicedetials
            VIEW_PRACTICEDETIALS,

            //All Privilege User
            FULL_ACCESS,

            //No access
            NO_ACCESS,

            //reminder
            ADD_REMINDER,
            VIEW_REMINDER,
            DELETE_REMINDER,
            EDIT_REMINDER,


        }

        fun savePrivileges(context: Context, privilegeArrList: ArrayList<Int>) {

            val str = privilegeArrList.joinToString(separator = ",")
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            pref.edit().putString(ModelUtil.rbacPreferencesString, str).commit()

        }

        fun savePrivilegesFromJson(jsonArray: JsonArray, context: Context) {

            val privilegeArr: ArrayList<Int> = ArrayList()


            jsonArray.forEach {

                val privilegeStr = it.asString

                when (privilegeStr) {
                    "full_access" -> {
                        privilegeArr.add(RBACPrivileges.FULL_ACCESS.ordinal)
                    }
                    "add_wellness" -> {
                        privilegeArr.add(RBACPrivileges.ADD_WELLNESS.ordinal)
                    }
                    "view_wellness" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_WELLNESS.ordinal)
                    }
                    "edit_wellness" -> {
                        privilegeArr.add(RBACPrivileges.EDIT_WELLNESS.ordinal)
                    }
                    "delete_wellness" -> {
                        privilegeArr.add(RBACPrivileges.DELETE_WELLNESS.ordinal)
                    }
                    "view_reports" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_REPORTS.ordinal)
                    }
                    "add_insurance" -> {
                        privilegeArr.add(RBACPrivileges.ADD_INSURANCE.ordinal)
                    }
                    "view_insurance" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_INSURANCE.ordinal)
                    }
                    "edit_insurance" -> {
                        privilegeArr.add(RBACPrivileges.EDIT_INSURANCE.ordinal)
                    }
                    "delete_insurance" -> {
                        privilegeArr.add(RBACPrivileges.DELETE_INSURANCE.ordinal)
                    }
                    "add_immune" -> {
                        privilegeArr.add(RBACPrivileges.ADD_IMMUNE.ordinal)
                    }
                    "view_immune" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_IMMUNE.ordinal)
                    }
                    "edit_immune" -> {
                        privilegeArr.add(RBACPrivileges.EDIT_IMMUNE.ordinal)
                    }
                    "delete_immune" -> {
                        privilegeArr.add(RBACPrivileges.DELETE_IMMUNE.ordinal)
                    }
                    "view_appointment" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_APPOINTMENT.ordinal)
                    }
                    "view_questionnaire" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_QUESTIONNAIRE.ordinal)
                    }
                    "view_visitsummary" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_VISITSUMMARY.ordinal)
                    }
                    "view_invoice" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_INVOICE.ordinal)
                    }
                    "view_messages" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_MESSAGES.ordinal)
                    }
                    "view_medications" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_MEDICATIONS.ordinal)
                    }
                   "view_documents" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_DOCUMENTS.ordinal)
                    }
                    "view_allergies" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_ALLERGIES.ordinal)
                    }
                    "view_immune" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_IMMUNE.ordinal)
                    }
                   "view_labresults" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_LABRESULTS.ordinal)
                    }
                    "view_procedure" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_PROCEDURE.ordinal)
                    }
                    "add_procedure" -> {
                        privilegeArr.add(RBACPrivileges.ADD_PROCEDURE.ordinal)
                    }
                    "view_usefulinfo" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_USEFULINFO.ordinal)
                    }
                    "view_clinicalsummary" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_CLINICALSUMMARY.ordinal)
                    }
                    "add_wellness" -> {
                        privilegeArr.add(RBACPrivileges.ADD_WELLNESS.ordinal)
                    }
                    "add_allergies" -> {
                        privilegeArr.add(RBACPrivileges.ADD_ALLERGIES.ordinal)
                    }
                    "add_medications" -> {
                        privilegeArr.add(RBACPrivileges.ADD_MEDICATIONS.ordinal)
                    }
                    "add_supplements" -> {
                        privilegeArr.add(RBACPrivileges.ADD_SUPPLEMENTS.ordinal)
                    }
                    "view_supplements" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_SUPPLEMENTS.ordinal)
                    }
                    "add_messages" -> {
                        privilegeArr.add(RBACPrivileges.ADD_MESSAGES.ordinal)
                    }
                    "add_documents" -> {
                        privilegeArr.add(RBACPrivileges.ADD_DOCUMENTS.ordinal)
                    }
                    "add_folder" -> {
                        privilegeArr.add(RBACPrivileges.ADD_FOLDERS.ordinal)
                    }
                    "view_flowsheetdata" -> {
                        privilegeArr.add(RBACPrivileges.VIEW_FLOWSHEETDATA.ordinal)
                    }
                    "add_flowsheetdata" -> {
                        privilegeArr.add(RBACPrivileges.ADD_FLOWSHEETDATA.ordinal)
                    }
                    "add_appointment" -> {
                        privilegeArr.add(RBACPrivileges.ADD_APPOINTMENT.ordinal)
                    }
                    "add_reminder" -> {
                        privilegeArr.add(RBACPrivileges.ADD_REMINDER.ordinal)
                    }
                    "edit_reminder"->{
                        privilegeArr.add(RBACPrivileges.EDIT_REMINDER.ordinal)
                    }
                    "delete_reminder" ->{
                        privilegeArr.add(RBACPrivileges.DELETE_REMINDER.ordinal)
                    }
                    "view_reminder" ->{
                        privilegeArr.add(RBACPrivileges.VIEW_REMINDER.ordinal)
                    }

                }
            }

            savePrivileges(context, privilegeArr)

        }

        fun checkPrivilege(privStr: RBACPrivileges, context: Context?): Boolean {

            if (BuildUtil.isCustomApp)
                return true
            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            val rbacPrefString = pref.getString(ModelUtil.rbacPreferencesString, null)
            val splitedArr = rbacPrefString?.trim()?.split(",")

            if (splitedArr != null) {
                for (str in splitedArr) {
                    if (Integer.parseInt(str) == RBACPrivileges.FULL_ACCESS.ordinal || Integer.parseInt(str) == privStr.ordinal) {
                        return true
                    }
                }
            }

            return false
        }

    }
}