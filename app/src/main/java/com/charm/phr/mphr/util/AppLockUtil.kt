package com.charm.phr.mphr.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.zoho.applock.clearEncryptionKeysFromKeyStore
import com.zoho.applock.decrypt
import com.zoho.applock.encrypt
import com.zoho.apptics.logger.AppticsLogger
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockUtil @Inject constructor(
    private val authManager: AuthManager
) {

    val TAG = "AppLockUtil"

    @Inject
    lateinit var sharedPreference: SharedPreferences

    fun mdEncrypt(
        encryptString: String,
        applicationContext: Context,
        prefName: String,
        authManager: AuthManager
    ): String {
        try {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val encryptedString = encrypt(applicationContext, null, encryptString)
            pref.edit().putString(prefName, encryptedString).commit()
            return encryptedString
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, e.stackTraceToString())
            return clearKeyStoreAndLogout(e, authManager)
        }
    }

    fun mdDecrypt(applicationContext: Context, prefName: String, authManager: AuthManager): String {
        try {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val encryptedString = pref.getString(prefName, null)
            if (encryptedString.isNullOrBlank()) return ""
            return decrypt(applicationContext, null, encryptedString, false)
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, e.stackTraceToString())
            return clearKeyStoreAndLogout(e, authManager)
        }
    }

    private fun clearKeyStoreAndLogout(e: Exception, authManager: AuthManager): String {
        Log.d(TAG, e.toString())
        clearEncryptionKeysFromKeyStore()
        if (sharedPreference.getBoolean(
                MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN,
                false
            )
        ) {
            authManager.logout(true)
        }
        return ""
    }

    fun getCharArrToEncryptDB(context: Context): CharArray {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val encryptedValStr = prefs.getString(ModelUtil.DBENCRYPT, null)
        try {

            // we will generate a random number for each user initially,
            // encrypt and use the same for db encryption
            if (encryptedValStr.isNullOrBlank()) {
                val rand = Random()
                val randomVal = rand.nextDouble()
                val str = randomVal.toString()
                mdEncrypt(str, context.applicationContext, ModelUtil.DBENCRYPT, authManager)
                return str.toCharArray()
            }
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, e.stackTraceToString())
        }
        val decodedStr: String = mdDecrypt(context.applicationContext, ModelUtil.DBENCRYPT, authManager)
        return decodedStr.toCharArray()
    }

}