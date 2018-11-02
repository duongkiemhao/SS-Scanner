package com.siliconstack.carscanner.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.siliconstack.carscanner.config.Config.Companion.DATE_TIME_PATTERN
import com.siliconstack.carscanner.view.utility.DateUtility
import java.util.*

@Entity()
data class MainModel (
        @PrimaryKey(autoGenerate = true)
        var id:Int,
        var scanText:String,
        var timestamp:Long,
        var type:Int,


        var locationID:Int?=null,
        var floorID:Int?=null,
        var operatorID:Int?=null,
        var bayNumber:String?=null
){
        var dateString:String?=null



}