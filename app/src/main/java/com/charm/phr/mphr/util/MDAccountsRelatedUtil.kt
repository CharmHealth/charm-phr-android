package com.charm.phr.mphr.util

class MDAccountsRelatedUtil {

    companion object {
        const val baseUrl = "https://phr2.charmtracker.com"

        const val oAuthScopes = "charmphr.patient.fullaccess.ALL,charmphr.careteam.ALL,charmphr.user.setting.ALL,charmphr.defaults.CREATE"//,charmphr.patient.wellness.ALL,charmphr.patient.questionnaire.ALL,charmphr.patient.condition.ALL,charmphr.patient.healthpassport.ALL

//        const val facilityLogoUrl = "$baseUrl/api/ehr/v1/facilities/logo?facility_id="
//
//        const val patientPhotoUrlPart1 = "$baseUrl/api/ehr/facilities/patients/photo?patientId="
//
//        const val patientPhotoUriPart2 = "&photoSize=PHOTO_120"

        const val IS_USER_SUCCESSFULLY_LOGGED_IN = "isUserSuccessfullyLoggedId"

        //        const val PRACTICE_NAME = "practiceName"
        const val INVALID_AUTHTOKEN_ERROR_CODE = 14


        const val hostName = "phr.charmtracker.com"

        const val zoomDomain = "zoom.us"
    }
}
