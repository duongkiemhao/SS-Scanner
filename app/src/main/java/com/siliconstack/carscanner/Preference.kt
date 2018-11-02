package com.siliconstack.carscanner

import com.chibatching.kotpref.KotprefModel
import com.marcinmoskala.kotlinpreferences.PreferenceHolder

import com.siliconstack.carscanner.model.FilterDialogModel
import com.siliconstack.carscanner.model.SelectionModel


object PreferenceSetting : PreferenceHolder() {


    object UserSetting : KotprefModel() {
        var token by stringPref("")
        var username by stringPref("")
        var password by stringPref("")
        var isRememberMe by booleanPref(false)
        var dealerKey by stringPref("")

    }


}
