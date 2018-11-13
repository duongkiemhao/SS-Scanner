package com.siliconstack.carscanner

import com.chibatching.kotpref.KotprefModel
import com.marcinmoskala.kotlinpreferences.PreferenceHolder

import com.siliconstack.carscanner.model.FilterDialogModel
import com.siliconstack.carscanner.model.SelectionModel


object PreferenceSetting : PreferenceHolder() {


    var locationSetting: FilterDialogModel? by bindToPreferenceFieldNullable("locationSetting")
    var floorSetting: FilterDialogModel? by bindToPreferenceFieldNullable("floorSetting")
    var nameSetting: FilterDialogModel? by bindToPreferenceFieldNullable("nameSetting")

}
