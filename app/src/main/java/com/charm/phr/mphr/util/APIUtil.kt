package com.charm.phr.mphr.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.charm.phr.mphr.MPHRApplication
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.ui.login.LoginActivity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.charm.phr.mphr.util.AuthenticationUtil.AuthFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs API calls once the user has a valid sign-in session (AuthenticationUtil). The access token itself is added
 * to each request by the authenticated OkHttp client (AuthenticationUtil.apiInterceptor, see AppModule); this
 * class checks the session first so a signed-out user is sent to the login page.
 */
@Singleton
class APIUtil @Inject constructor(
    val appContext: Context,
    val authManager: AuthManager,
    val sharedPreferences: SharedPreferences,
) {

    suspend fun initiateSynchronizeRequest(call: Call<JsonElement>): Any? {
        if (!BuildUtil.isOAuthEnabled || authManager.skipAuth) {
            return authManager.synchronizeAPICall(call)
        }
        if (AuthenticationUtil.ensureAccessToken() != null) {
            return APIResponseError(null, null, appContext.getString(R.string.error_no_valid_user_found))
        }
        return withContext(Dispatchers.IO) { authManager.synchronizeAPICall(call) }
    }

    fun initiateRequest(call: Call<JsonElement>?, callback: MDCallback) {
        if (BuildUtil.isOAuthEnabled && !authManager.skipAuth) {
            withValidSession(callback) { call?.enqueue(callback) }
        } else {
            call?.enqueue(callback)
        }
    }

    fun initiateRequestForMisc(call: Call<ResponseBody>, callback: MDCallbackForMisc) {
        if (BuildUtil.isOAuthEnabled) {
            withValidSession(callback) { call.enqueue(callback) }
        } else {
            call.enqueue(callback)
        }
    }

    fun getToken(callback: Any) {
        withValidSession(callback) {
            if (callback is MDCallback) {
                callback.onMDSuccess(JsonObject())
            }
        }
    }

    /** Runs [onReady] once a token is available, otherwise logs out or reports the failure. */
    private fun withValidSession(callback: Any, onReady: () -> Unit) {
        AuthenticationUtil.scope.launch {
            val failure = AuthenticationUtil.ensureAccessToken()
            if (failure == null) {
                onReady()
            } else {
                checkForLogoutOrSendFailureCallback(failure.requiresSignIn, failure, callback)
            }
        }
    }

    private fun checkForLogoutOrSendFailureCallback(
        toLogout: Boolean,
        error: AuthFailure,
        callback: Any
    ) {
        if (sharedPreferences.getBoolean(
                MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN,
                false
            )
        ) {
            if (toLogout) {
                val application = appContext as MPHRApplication
                if (application.currentActivity !is LoginActivity) {
                    authManager.logout()
                    val intent = Intent(appContext, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(ModelUtil.apiTokenFailed, true)
                    appContext.startActivity(intent)
                } else {
                    checkAndSendFailureCallBack(callback, error)
                }
            } else {
                checkAndSendFailureCallBack(callback, error)
            }
        }
    }

    private fun checkAndSendFailureCallBack(callback: Any, error: AuthFailure) {
        val apiError = APIResponseError(null, null, getErrorDescription(error))
        if (callback is MDCallbackForMisc) {
            callback.onMDFailure(apiError)
        } else if (callback is MDCallback) {
            callback.onMDFailure(apiError)
        } // else Handel else block if new call back is created
    }

    private fun getErrorDescription(error: AuthFailure): String = when (error) {
        AuthFailure.NETWORK -> appContext.getString(R.string.io_exception)
        else -> appContext.getString(R.string.something_went_wrong)
    }
}
