package com.siliconstack.carscanner.view.ui.base

import android.os.Bundle
import android.support.v4.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.siliconstack.carscanner.view.helper.DialogHelper

open class BaseFragment:Fragment(){
    lateinit var progressDialog: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = DialogHelper.materialProgressDialog(context!!)
    }


}