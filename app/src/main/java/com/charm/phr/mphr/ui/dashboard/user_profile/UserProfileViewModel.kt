package com.charm.phr.mphr.ui.dashboard.user_profile

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class UserProfileViewModel : ViewModel() {

    var messageRestrictionList: String = ""
    val imageReloadKey = mutableIntStateOf(0)

}