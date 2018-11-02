package com.siliconstack.carscanner.view.listeners

import com.siliconstack.carscanner.model.FilterDialogModel

interface FilterDialogListener{
    fun onSelectOk(filterDialogModel: FilterDialogModel)
}