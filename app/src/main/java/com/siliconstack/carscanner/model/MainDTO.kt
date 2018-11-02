package com.siliconstack.carscanner.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable
import com.siliconstack.carscanner.config.Config.Companion.DATE_TIME_PATTERN
import com.siliconstack.carscanner.view.utility.DateUtility
import java.util.*
import kotlin.Comparator


open class MainDTO() : Parcelable,Comparator<MainDTO> {
        override fun compare(o1: MainDTO?, o2: MainDTO?): Int {
                return  (o1!!.timestamp!! - o2!!.timestamp!!).toInt()
        }



        var id:Int? = null
        var scanText:String?=null
        var timestamp:Long?=null
        var type:Int?=null


        var locationID:Int?=null
        var locationName:String?=null
        var floorID:Int?=null
        var floorName:String?=null
        var operatorID:Int?=null
        var operatorName:String?=null
        var bayNumber:String?=null
        var dateString:String?=null

        var isSelected:Boolean=false

        constructor(parcel: Parcel) : this() {
                id = parcel.readValue(Int::class.java.classLoader) as? Int
                scanText = parcel.readString()
                timestamp = parcel.readValue(Long::class.java.classLoader) as? Long
                type = parcel.readValue(Int::class.java.classLoader) as? Int
                locationID = parcel.readValue(Int::class.java.classLoader) as? Int
                locationName = parcel.readString()
                floorID = parcel.readValue(Int::class.java.classLoader) as? Int
                floorName = parcel.readString()
                operatorID = parcel.readValue(Int::class.java.classLoader) as? Int
                operatorName = parcel.readString()
                bayNumber = parcel.readString()
                dateString = parcel.readString()
                isSelected = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeValue(id)
                parcel.writeString(scanText)
                parcel.writeValue(timestamp)
                parcel.writeValue(type)
                parcel.writeValue(locationID)
                parcel.writeString(locationName)
                parcel.writeValue(floorID)
                parcel.writeString(floorName)
                parcel.writeValue(operatorID)
                parcel.writeString(operatorName)
                parcel.writeString(bayNumber)
                parcel.writeString(dateString)
                parcel.writeByte(if (isSelected) 1 else 0)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<MainDTO> {
                override fun createFromParcel(parcel: Parcel): MainDTO {
                        return MainDTO(parcel)
                }

                override fun newArray(size: Int): Array<MainDTO?> {
                        return arrayOfNulls(size)
                }
        }


}