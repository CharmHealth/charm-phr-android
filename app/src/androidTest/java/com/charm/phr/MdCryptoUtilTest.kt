package com.charm.phr

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.charm.phr.mphr.util.MdCryptoUtil
import junit.framework.Assert.assertNull
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MdCryptoUtilTest {

    @Test
    fun testEncryptionAndDecryption() {
        val context = getInstrumentation().targetContext
        val key = "testEncryptKey"
        val value = "testValue"

        val encryptionResult = MdCryptoUtil.encrypt(key, value, context)
        assertEquals(true, encryptionResult)

        val decryptedValue = MdCryptoUtil.decrypt(key, context)
        assertEquals(value, decryptedValue)
    }

    @Test
    fun testDecryptionWithInvalidKey() {
        val context = getInstrumentation().targetContext
        val key = "invalidKey"
        val decryptedValue = MdCryptoUtil.decrypt(key, context)
        assertEquals(null, decryptedValue)
    }


    @Test
    fun testDecryptionWithNullKey() {
        val context = getInstrumentation().targetContext
        val key: String? = null

        val decryptedValue = MdCryptoUtil.decrypt(key ?: "", context)
        assertNull(decryptedValue)
    }

    @Test
    fun testEncryptionAndDecryptionWithEmptyString() {
        val context = getInstrumentation().targetContext
        val key = "testKeyEmpty"
        val value = ""

        val encryptionResult = MdCryptoUtil.encrypt(key, value, context)
        assertEquals(true, encryptionResult)

        val decryptedValue = MdCryptoUtil.decrypt(key, context)
        assertEquals(value, decryptedValue)
    }

    @Test
    fun testEncryptionAndDecryptionWithSpecialCharacters() {
        val context = getInstrumentation().targetContext
        val key = "testKeySpecial"
        val value = "!@#\$%^&*()_+{}|:\"<>?[]\\;',./`~"

        val encryptionResult = MdCryptoUtil.encrypt(key, value, context)
        assertEquals(true, encryptionResult)

        val decryptedValue = MdCryptoUtil.decrypt(key, context)
        assertEquals(value, decryptedValue)
    }

}