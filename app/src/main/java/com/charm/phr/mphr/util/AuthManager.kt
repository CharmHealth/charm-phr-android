package com.charm.phr.mphr.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.DataWithBoolean
import com.charm.phr.mphr.model.MenuList
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.mPHRDB
import com.charm.phr.mphr.service.notification.UnRegisterDeviceWorker
import com.charm.phr.mphr.ui.login.LoginActivity
import com.charm.phr.mphr.util.MDAccountsRelatedUtil.Companion.INVALID_AUTHTOKEN_ERROR_CODE
import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    val appContext: Context,
    val sharedPreferences: SharedPreferences,
    val executor: Executor,
    val db: Provider<mPHRDB>
) {

    var isBasic = false



    var skipAuth = false

    var authStatusLive = MutableLiveData<AuthStatus>()

    var authToken: String? = null
        set(value) {
            //
            field = value
            updateAuthStatus()
        }
    var reachability: Boolean = false
        set(value) {
            //
            field = value
            updateAuthStatus()
        }

    var patientList: ArrayList<Patient> = arrayListOf()


    var menuList: MenuList = MenuList()

    var selectedPatientLive = MutableLiveData<Patient>()

    var selectedPatientSection: SectionConfigList? = null

    var questionnaireHPList: ArrayList<PhysicianTemplates>? = null
    private var networkList = arrayListOf<DataWithBoolean>()

    var isAllowStatementPayment = true


    private val TAG = "AuthManager"

    var privilegeArrList: ArrayList<Int> = ArrayList()

    init {


        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder()


        //reachability = connectivityManager.isDefaultNetworkActive

        restoreSavedData()

        connectivityManager.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                /**
                 * @param network
                 */
                override fun onAvailable(network: Network) {

                    val networkId = network.toString()
                    changeNetwork(networkId, true)

                    reachability = true
                    updateAuthStatus()
                }

                /**
                 * @param network
                 */
                override fun onLost(network: Network) {

                    val networkId = network.toString()

                    changeNetwork(networkId, false)

                    val networkData = networkList.filter { it.value }.firstOrNull()
                    reachability = networkData != null

                    updateAuthStatus()


                }
            }

        )
        Log.d(TAG, "Initialisation")
    }

    fun switchSelectedPatientForId(patientId: String) {
        val patientListStr = sharedPreferences.getString(ModelUtil.patientList, "") ?: ""
        if (patientListStr.isNotBlank()) {
            patientList = MDParsingHelper.formPatientList(patientListStr)

            if (patientId.isNotBlank()) {
                for (patient in patientList) {
                    if (patient.practicePatientId == patientId) {
                        updateSelectedPatient(patient)
                        break
                    }
                }
            }
        }
    }

    fun restoreSavedData() {


        val patientListStr = sharedPreferences.getString(ModelUtil.patientList, "") ?: ""
        if (patientListStr.isNotBlank()) {
            patientList = MDParsingHelper.formPatientList(patientListStr)

            val selectedPatientIdStr =
                sharedPreferences.getString(ModelUtil.selectedPatientId, "") ?: ""

            if (selectedPatientIdStr.isNotBlank()) {
                for (patient in patientList) {
                    if (patient.practicePatientId == selectedPatientIdStr) {
                        updateSelectedPatient(patient)
                        break
                    }
                }
            }
        }

        val menuListStr = sharedPreferences.getString(ModelUtil.menuList, "") ?: ""

        if (menuListStr.isNotBlank()) {
            menuList = MDParsingHelper.formMenuListObj(menuListStr)
        }
        setPrivilegeData()
    }

    fun setPrivilegeData() {

        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
        val rbacPrefString = pref.getString(ModelUtil.rbacPreferencesString, null)
        val splitedArr = rbacPrefString?.trim()?.split(",")
        val prArrayList: ArrayList<Int> = ArrayList()

        if (splitedArr != null) {
            for (str in splitedArr) {
                prArrayList.add(Integer.parseInt(str))
            }

        }
        privilegeArrList = prArrayList

    }


    fun checkPrivilege(privStr: RbacUtil.Companion.RBACPrivileges): Boolean {
        return privilegeArrList.contains(RbacUtil.Companion.RBACPrivileges.FULL_ACCESS.ordinal) || privilegeArrList.contains(
            privStr.ordinal
        )
    }

    private fun changeNetwork(networkId: String, isAvailable: Boolean) {

        val networkPair = networkList.filter { it.key == networkId }.firstOrNull()

        if (networkPair != null) {
            networkPair.value = isAvailable
        } else {
            networkList.add(DataWithBoolean(networkId, isAvailable))
        }
    }

    fun updateSelectedPatient(patient: Patient) {
        sharedPreferences.edit().putString(ModelUtil.selectedPatientId, patient.practicePatientId)
            .commit()

        selectedPatientSection = getSelectedPatientSection(patient.practicePatientId ?: "")

        selectedPatientLive.postValue(patient)

    }

    fun updateSelectedPatientSection(patientId: String, sectionConfigList: SectionConfigList) {
        sharedPreferences.edit()
            .putString(ModelUtil.patientSectionPrefix + patientId, sectionConfigList.toString)
            .commit()
        selectedPatientSection = sectionConfigList
    }

    fun getSelectedPatientSection(patientId: String): SectionConfigList? {
        val resp = sharedPreferences.getString(ModelUtil.patientSectionPrefix + patientId, "")
        return if (resp.isNullOrBlank()) {
            null
        } else {
            MDParsingHelper.formPatientSection(resp.parseStringAsJsonObject() ?: JsonObject())
        }
    }

    fun updateAuthStatus() {

        //To update authmanager.authstatusLive.value


        if (reachability) {
            authStatusLive.postValue(AuthStatus.USER_ONLINE)

        } else {
            authStatusLive.postValue(AuthStatus.USER_OFFLINE)
        }

        try {
            //Adding Inorder to update Immediately

            if (reachability) {
                Log.d(TAG, "USER_ONLINE")
                authStatusLive.value = AuthStatus.USER_ONLINE
            } else {
                authStatusLive.value = AuthStatus.USER_OFFLINE
            }
        } catch (e: Exception) {
            //
            Log.d("Ignore", "postValue exception")
        }
    }

    fun isUserOnline(): Boolean = authStatusLive.value == AuthStatus.USER_ONLINE
    //fun isUserOffline():Boolean = authStatusLive.value == AuthStatus.USER_OFFLINE


    fun logout(isFromEncryption: Boolean = false): Boolean {
        //TODO:

        //if (authStatusLive.value == AuthStatus.USER_ONLINE) {
        try {

            logoutUserForSignIn(isFromEncryption)
            afterLogoutOpertaion(isFromEncryption)


        } catch (e: java.lang.Exception) {
            Log.d("MDLogout", e.toString())
        }
        // }


        return true
    }

//    fun logoutWithProgress(): LiveData<Boolean> {
//        return logoutUserForSignIn()
//    }


    fun logoutUserForSignIn(isFromEncryption: Boolean): LiveData<Boolean> {
        val isSuccess = MutableLiveData<Boolean>()

        AuthenticationUtil.scope.launch {
            // Revokes the refresh token, then deletes the local session even offline.
            if (AuthenticationUtil.signOut()) {
                Log.d("MDLogout", "success")
                sharedPreferences.edit().putBoolean(ModelUtil.prefIsLoggedOut, true).commit()
                if (!BuildUtil.isCustomApp && !BuildUtil.isThalassemia) {
                    AppticsUtil.addEvent(MdEventGroup.Account, MdEvent.LogoutOnSuccess)
                    if (isFromEncryption) {
                        AppticsUtil.addEvent(MdEventGroup.Account, MdEvent.LogoutOnKeyStoreCorrupt)
                    }
                }
                isSuccess.postValue(true)
            } else {
                Log.d("MDLogout", "failed")
                isSuccess.postValue(false)
            }
        }

        return isSuccess
    }

    fun afterLogoutOpertaion(isFromEncryption: Boolean) {
        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = pref.edit()

        //Priority: Unregister device worker should be called before clearing preferences
        if (BuildUtil.isPushNotificationEnabled) {
            UnRegisterDeviceWorker.enqueueUnregisterDeviceWork(appContext)
        }

        editor.remove(ModelUtil.IAM_AUTHTOKEN)
        editor.remove(ModelUtil.patientList)
        editor.remove(ModelUtil.selectedPatientId)
        editor.remove(ModelUtil.menuList)
        editor.remove(ModelUtil.PASSCODE)
        editor.remove(ModelUtil.lockType)
        editor.remove(ModelUtil.applockType)
        editor.remove(ModelUtil.WRONG_PASSCODE_ATTEMPT_COUNT)
//        editor.remove(KeyStoreUtil.PASSCODE_VECTOR)
//       Note: PASSCODE and ENCRYPTED_KEYS are removed on afterNewUserLoginOperation()
//        editor.remove(AUTHTOKEN_VECTOR)
//        editor.remove(ModelUtil.bluetoothDevice)
        editor.remove(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN)
        editor.remove(ModelUtil.isAppointmentReminderPeriodic)
        editor.remove(ModelUtil.isPrescriptionReminderSchedulerStarted)
        editor.remove(ModelUtil.isReminderDataFetched)
        editor.remove(ModelUtil.lastAlarmScheduleTime)
        //Never remove DB Encrypt key as we are not deleting the DB instance on logout.This key is required to access the DB
        // editor.remove(ModelUtil.DBENCRYPT)
        if (isFromEncryption) {
            //To be remove as DB key got corrupted and KeyStoreUtil Cleared
            editor.remove(ModelUtil.DBENCRYPT)
            //Delete db as keystore got corrupted we will not be able to access db
            appContext.applicationContext.deleteDatabase(ModelUtil.charmMPHRDb)
        }
        editor.commit()
        authToken = null
        // check clear all preference
        //Reminder Handling
        NotificationUtil.clearAllNotification(appContext)

        editor.remove(ModelUtil.installationIdForNotification)
        editor.remove(ModelUtil.isDeviceRegisteredForNotification)
        editor.remove(ModelUtil.unregisteredDevicePayload)
        editor.remove(ModelUtil.registeredPatientIdForNotification)
        editor.remove(ModelUtil.fcmTokenForNotification)



        cancelAllWorker()
        if (!isFromEncryption) {
            cancelPastScheduledAlarm()
            db.get().clearAllTables()
        }
    }

    fun cancelAllWorker() {
        WorkManager.getInstance(appContext).cancelAllWorkByTag(ModelUtil.scheduleReminderTAG)
        WorkManager.getInstance(appContext).cancelAllWorkByTag(ModelUtil.getReminderDataTAG)
    }

    private fun cancelPastScheduledAlarm() {
        //Get All the scheduled Alarm from FrequencyAlarmMap
        val scheduledAlarm = db.get().miscDao().getAllFrequencyAlarmMap()
        //Cancel All the Scheduled Alarm to avoid duplication
        cancelScheduledAlarm(scheduledAlarm as ArrayList<FrequencyAlarmMap>)
        //Remove all the existing alarm from the FrequencyAlarmMap
        db.get().miscDao().deleteAllFrequencyAlarmMap()
    }

    private fun cancelScheduledAlarm(scheduledAlarm: ArrayList<FrequencyAlarmMap>) {
        val title = appContext.getString(R.string.notification_title)
        val message = appContext.getString(R.string.notification_message)
        scheduledAlarm.forEach { frequencyAlarmMap ->
            NotificationUtil.cancelNotification(
                appContext,
                title,
                message,
                frequencyAlarmMap.localFrequencyAlarmMapId.toInt()
            )
        }
    }

    fun synchronizeAPICall(webCall: Call<JsonElement>): Any? {
        try {
            val webResp = webCall.execute()
            if (webResp.isSuccessful) {
                val responseBody = webResp.body()?.asJsonObject
                val code = responseBody?.get("code")?.asInt
                val success = responseBody?.get("error")?.asBoolean == false
                return if ((code == 0 || success)) {
                    responseBody
                } else {
                    APIResponseError(code, webResp.code(), responseBody?.get("message")?.asString ?: responseBody?.get("error_message")?.asString)
                }
            } else {

                return apiErrorResponseHandeler(webResp) ?: APIResponseError(
                    null,
                    null,
                    appContext.getString(R.string.conversion_error)
                )

                /* try {

                     val jsonObject = JsonParser().parse(webResp.errorBody()?.string()).asJsonObject
                     val code = jsonObject.get("code")?.asInt

                     return if (code == MDAccountsRelatedUtil.INVALID_AUTHTOKEN_ERROR_CODE) //INVALID AUTHTOKEN CODE
                     {
                         logout()
                         val intent = Intent(appContext, LoginActivity::class.java)
                         intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                         intent.putExtra(ModelUtil.authTokenFailed, true)
                         appContext.startActivity(intent)
                         APIResponseError(code , null , appContext.getString(R.string.no_authtoken))

                     }
                     else{
                         APIResponseError(code, null, null)
                     }

                 } catch (e: Exception) {

                     return  APIResponseError(null, null,appContext.getString(R.string.api_not_success_error))

                 }*/


            }

        } catch (e: Exception) {
            return APIResponseError(
                null, null, when (e) {
                    is IOException -> appContext.getString(R.string.io_exception)
                    is SocketTimeoutException -> appContext.getString(R.string.socket_timeout_exception)
                    else -> appContext.getString(R.string.conversion_error)
                }
            )
        }
    }

    fun apiErrorResponseHandeler(response: retrofit2.Response<JsonElement>): APIResponseError? {
        //when response is unSuccessful


        var apiResp: APIResponseError?

        try {

            val element = response.errorBody()?.string().parseStringAsJsonObject()
            var jsonObject: JsonObject? = null

            if (jsonObject.isNotJsonNull()) {
                jsonObject = element?.asJsonObject
            }

            Log.d("apiErrorResHandler", "jsonObject:$jsonObject")
            val code = jsonObject?.get("code")?.asInt

            if (code == INVALID_AUTHTOKEN_ERROR_CODE && sharedPreferences.getBoolean(
                    MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN,
                    false
                )
            ) //INVALID AUTHTOKEN CODE
            {
                logout()
                val intent = Intent(appContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(ModelUtil.apiTokenFailed, true)
                appContext.startActivity(intent)

                apiResp = null

            } else {
                apiResp = APIResponseError(code, null, jsonObject?.get("message")?.asString)
            }

        } catch (e: Exception) {

            apiResp = APIResponseError(null, null, ModelUtil.unknownError)

        }

        return apiResp

    }

    fun apiErrorResponseHandlerForJsonElement(
        response: retrofit2.Response<JsonElement>,
        errorRespCode: Int? = null,
        message: String?
    ): APIResponseError {
        //when response is unSuccessful

        var apiResp: APIResponseError

        try {

            if (response.isSuccessful) {

                val responseObj = response.body()?.asJsonObject
                val code = responseObj?.get("code")?.asInt
                if (code == MDAccountsRelatedUtil.INVALID_AUTHTOKEN_ERROR_CODE && !BuildUtil.isOAuthEnabled && sharedPreferences.getBoolean(
                        MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN,
                        false
                    )
                ) { //INVALID AUTHTOKEN CODE

                    logout()
                    val intent = Intent(appContext, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(ModelUtil.apiTokenFailed, true)
                    appContext.startActivity(intent)

                    apiResp = APIResponseError(null, null, null)

                } else apiResp =
                    APIResponseError(code, response.code(), responseObj?.get("message")?.asString)

            } else {


                if (response.raw().header("content-type")?.contains("application/json") == true) {


                    var code: Int? = null
                    val errorBody = response.errorBody()?.string().parseStringAsJsonObject()
                    var jsonObject: JsonObject? = null
                    if (errorBody.isNotJsonNull()) {
                        jsonObject = errorBody?.asJsonObject
                    }

                    code = if (errorRespCode != null) {
                        errorRespCode
                    } else {
                        jsonObject?.get("code")?.asInt

                    }

                    if (code == INVALID_AUTHTOKEN_ERROR_CODE && !BuildUtil.isOAuthEnabled && sharedPreferences.getBoolean(
                            MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN,
                            false
                        )
                    ) { //INVALID AUTHTOKEN CODE

                        logout()
                        val intent = Intent(appContext, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(ModelUtil.apiTokenFailed, true)
                        appContext.startActivity(intent)

                        apiResp = APIResponseError(code, null, message)

                    } else apiResp = APIResponseError(code, null, message)
                } else {


                    val webRespCode = response.code()

                    /* if (webRespCode == 401 && sharedPreferences.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN , false)){
                         val application = appContext as MPHRApplication
                         if (application.currentActivity !is LoginActivity) {
                             logout()
                             val intent = Intent(appContext, LoginActivity::class.java)
                             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                             intent.putExtra(ModelUtil.apiTokenFailed, true)
                             intent.putExtra(ModelUtil.isOldAppIntent, false)
                             appContext.startActivity(intent)
                         }
                     }*/
                    apiResp = APIResponseError(null, webRespCode, message)
                }


            }

        } catch (e: Exception) {
            apiResp = APIResponseError(null, null, "Error Occurred")
        }

        return apiResp

    }


    fun apiErrorResponseHandlerForResponseBody(
        response: retrofit2.Response<ResponseBody>,
        errorRespCode: Int? = null
    ): APIResponseError {
        //when response is unSuccessful

        var apiResp: APIResponseError

        try {

            if (response.isSuccessful) {


                if (response.raw().header("content-type")
                        ?.contains("application/json", ignoreCase = true) == true
                ) {


                    val responseObj = response.body()?.string().parseStringAsJsonObject()

                    val code = responseObj?.get("code")?.asInt
                    if (code == MDAccountsRelatedUtil.INVALID_AUTHTOKEN_ERROR_CODE && !BuildUtil.isOAuthEnabled) { //INVALID AUTHTOKEN CODE

                        logout()
                        val intent = Intent(appContext, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(ModelUtil.apiTokenFailed, true)
                        appContext.startActivity(intent)

                        apiResp = APIResponseError(null, null, null)

                    } else apiResp =
                        APIResponseError(
                            code,
                            response.code(),
                            responseObj?.get("message")?.asString
                        )
                } else {
                    apiResp = APIResponseError(null, null, null)
                }

            } else {

                val errorBody = response.errorBody()?.string().parseStringAsJsonObject()
                var jsonObject: JsonObject? = null
                if (errorBody.isNotJsonNull()) {
                    jsonObject = errorBody?.asJsonObject
                }

                val code = if (errorRespCode != null) {
                    errorRespCode
                } else {
                    jsonObject?.get("code")?.asInt

                }

                if (code == INVALID_AUTHTOKEN_ERROR_CODE && !BuildUtil.isOAuthEnabled) { //INVALID AUTHTOKEN CODE

                    logout()
                    val intent = Intent(appContext, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(ModelUtil.apiTokenFailed, true)
                    appContext.startActivity(intent)

                    apiResp = APIResponseError(code, null, null)

                } else apiResp = APIResponseError(code, null, null)

            }

        } catch (e: Exception) {
            apiResp = APIResponseError(null, null, "Error Occurred")
        }

        return apiResp

    }


    companion object {

        fun oAuthAppInit(appContext: Context) {
            if (BuildUtil.isOAuthEnabled) {
                AuthenticationUtil.init(appContext)
            }
        }

        fun cancelAppCalls(okHttpClient: OkHttpClient) {
            okHttpClient.dispatcher.cancelAll()
        }

    }


}




