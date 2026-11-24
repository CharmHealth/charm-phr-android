package com.charm.phr.mphr.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.preference.PreferenceManager
import com.zoho.apptics.logger.AppticsLogger
import com.charm.phr.mphr.util.BuildUtil
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object MdCryptoUtil {

    private const val TAG = "MdCryptoUtil"
    private const val PREF_NAME = "md_secure_prefs"
    private const val KEY_ALIAS = "md_secure_key"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128

    fun generateNewKeyString(length: Int = 32): String {
        return try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val secretKey = keyGenerator.generateKey()
            val hexString = StringBuilder()
            for (byte in secretKey.encoded) {
                hexString.append(String.format("%02x", byte))
            }

            val keyString = hexString.toString()
            if (keyString.length > length) {
                keyString.substring(0, length)
            } else {
                keyString.padEnd(length, '0')
            }
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.d(TAG, "Error generating new key string $e")
            ""
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (keyStore.containsAlias(KEY_ALIAS)) {
                val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                secretKeyEntry.secretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, "Error getting secret key $e")
            null
        }
    }

    private fun getCipher(): Cipher? {
        return try {
            Cipher.getInstance(AES_MODE)
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, "Error getting cipher instance $e")
            null
        }
    }

    fun encrypt(key: String, value: String, context: Context): Boolean {
        return try {
            val secretKey = getSecretKey() ?: throw IllegalStateException("Secret key is null")
            val cipher = getCipher() ?: throw IllegalStateException("Cipher instance is null")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedValue = cipher.doFinal(value.toByteArray())
            val encryptedValueWithIv = iv + encryptedValue
            val encryptedValueBase64 = Base64.encodeToString(encryptedValueWithIv, Base64.DEFAULT)
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            pref.edit().putString(key, encryptedValueBase64).commit()
            true
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, "Encryption error $e" )
            false
        }
    }

    fun decrypt(key: String, context: Context): String? {
        return try {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val encryptedValueBase64 = pref.getString(key, null) ?: return null

            val encryptedValueWithIv = Base64.decode(encryptedValueBase64, Base64.DEFAULT)
            val iv = encryptedValueWithIv.copyOfRange(0, IV_SIZE)
            val encryptedValue = encryptedValueWithIv.copyOfRange(IV_SIZE, encryptedValueWithIv.size)

            val secretKey = getSecretKey() ?: throw IllegalStateException("Secret key is null")
            val cipher = getCipher() ?: throw IllegalStateException("Cipher instance is null")
            val spec = GCMParameterSpec(TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedValue = cipher.doFinal(encryptedValue)
            val decryptedString = String(decryptedValue)
            decryptedString
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, "Decryption error $e")
            null
        }
    }

    fun checkAndCreateDbEncryptionKeys(context: Context) {

        try {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val encryptionKey = pref.getString(ModelUtil.DbEncryptionKey, null)
            if (encryptionKey != null) {
                return
            }

            val newKey = generateNewKeyString()
            //Encrypt and save in preferences
            encrypt(key = ModelUtil.DbEncryptionKey, value = newKey, context = context)
        } catch (e: Exception) {
            if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, "Encryption error $e")
        }

    }
}