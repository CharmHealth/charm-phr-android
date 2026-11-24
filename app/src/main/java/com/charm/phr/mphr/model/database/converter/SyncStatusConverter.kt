package com.charm.phr.mphr.model.database.converter

import androidx.room.TypeConverter
import com.charm.phr.mphr.model.database.entity.SyncOperation
import com.charm.phr.mphr.model.database.entity.SyncStatus

class SyncStatusConverter {
    //
    @TypeConverter
    fun syncStatusToInt(syncStatus: SyncStatus?): Int? {
        return when (syncStatus) {
            SyncStatus.NOT_SYNCED -> 1
            SyncStatus.SYNCING -> 2
            SyncStatus.SYNC_SUCCESS -> 3
            SyncStatus.SYNC_FAILURE -> 4

            else -> null
        }
    }

    @TypeConverter
    fun intToSyncStatus(intValue: Int?): SyncStatus? {
        return when (intValue) {
            1 -> SyncStatus.NOT_SYNCED
            2 -> SyncStatus.SYNCING
            3 -> SyncStatus.SYNC_SUCCESS
            4 -> SyncStatus.SYNC_FAILURE
            else -> null
        }
    }
}

class SyncOperationConverter {
    //
    @TypeConverter
    fun syncOperationToInt(syncOperation: SyncOperation?): Int? {
        return when (syncOperation) {
            SyncOperation.ADD -> 1
            SyncOperation.UPDATE -> 2
            SyncOperation.DELETE -> 3
            else -> null
        }
    }

    @TypeConverter
    fun intToSyncOperation(intValue: Int?): SyncOperation? {
        return when (intValue) {
            1 -> SyncOperation.ADD
            2 -> SyncOperation.UPDATE
            3 -> SyncOperation.DELETE
            else -> null
        }
    }
}