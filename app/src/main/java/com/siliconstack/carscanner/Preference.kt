package com.siliconstack.carscanner

import com.chibatching.kotpref.KotprefModel
import com.marcinmoskala.kotlinpreferences.PreferenceHolder

import com.siliconstack.carscanner.model.FilterDialogModel
import com.siliconstack.carscanner.model.SelectionModel


object PreferenceSetting : PreferenceHolder() {


    var locationPosition: Int by bindToPreferenceField(0)
    var floorPosition: Int by bindToPreferenceField(0)
    var namePosition: Int by bindToPreferenceField(0)

}
