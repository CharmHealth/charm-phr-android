package com.charm.phr.mphr.util

import androidx.lifecycle.ViewModel

abstract class MDAddBaseViewModel : ViewModel(){

    var toEdit = false
    var practiceId = ""
    var isFilled = false
    var isDataAdded = false
}