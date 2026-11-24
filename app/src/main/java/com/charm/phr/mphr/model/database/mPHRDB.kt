package com.charm.phr.mphr.model.database

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.charm.phr.mphr.model.database.dao.MiscDao
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.util.MdCryptoUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.BuildUtil
import com.zoho.apptics.logger.AppticsLogger
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Created by deepaj-6611 on 03/11/22.
 */

@Database(
    entities = [Reminder::class, ReminderMap::class, ReminderFrequencyMap::class, ReminderHistory::class, FrequencyAlarmMap::class /*ReminderTemplates::class*/, Medications::class, Supplements::class],
    version = 1,
    exportSchema = false
)

abstract class mPHRDB : RoomDatabase() {
    //
//    abstract fun consultationDao(): ConsultationDao
    abstract fun miscDao(): MiscDao

    companion object {

        val TAG = "mPHRDB"

        @Volatile
        private var INSTANCE: mPHRDB? = null

        fun getInstance(context: Context): mPHRDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): mPHRDB {
            val databaseBuilder = Room.databaseBuilder(context.applicationContext, mPHRDB::class.java, ModelUtil.charmMPHRDBName)
            try {
                checkAndCreateDbEncryptionKeys(context.applicationContext)
                val decryptedKey = MdCryptoUtil.decrypt(ModelUtil.DbEncryptionKey, context.applicationContext)
                if (decryptedKey != null) {
                    val keyByteArray: ByteArray = decryptedKey.toByteArray(Charsets.UTF_8)
                    val supportFactory = SupportOpenHelperFactory(keyByteArray)
                    databaseBuilder.openHelperFactory(supportFactory) // added for db encryption
                } else {
                    throw IllegalArgumentException("Decryption of encryptedDBKey returned null")
                }
            } catch (e: Exception) {
                if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, e.stackTraceToString())
            }
            return databaseBuilder.allowMainThreadQueries()
                .build()
        }
    }
}

fun checkAndCreateDbEncryptionKeys(context: Context) {

    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val encryptionKey = pref.getString(ModelUtil.DbEncryptionKey, null)
    if (encryptionKey != null) {
        return
    }

    val newKey = MdCryptoUtil.generateNewKeyString()
    //Encrypt and save in preferences
    MdCryptoUtil.encrypt(key = ModelUtil.DbEncryptionKey, value = newKey, context = context)

}