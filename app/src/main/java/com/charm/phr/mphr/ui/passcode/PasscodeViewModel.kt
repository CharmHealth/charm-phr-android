package com.charm.phr.mphr.ui.passcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.SecurityUtil
import com.google.gson.JsonArray

class PasscodeViewModel(private val miscRepository: MiscRepository) : ViewModel() {

    var passcodeType: Int = PasscodeActivity.setPasscode
    var secondTime = false
    var firstTimeVal = ""
    var userType: String? = null
    var isFirstTime = true
    var blockBackFunction = false
    var allowKeyBordPopUp = true
    var allowToStartActivity = true
    var from: String = ""
    var confirmUserSuccessfullyLoggedIn = false
    var pcLastUserBackPressedTime = 0L
    var lockType: SecurityUtil.LockType? = SecurityUtil.LockType.PinLock

    fun fetchPrivileges(): LiveData<Pair<Boolean, JsonArray?>> {
        return miscRepository.fetchPrivileges()
    }
}