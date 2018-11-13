package com.siliconstack.carscanner.view.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.siliconstack.carscanner.R
import com.siliconstack.carscanner.config.Constant
import com.siliconstack.carscanner.model.MainDTO
import com.siliconstack.carscanner.view.utility.DateUtility
import java.util.*

class CustomInfoWindowGoogleMap:GoogleMap.InfoWindowAdapter{
    lateinit var context: Context
    constructor(context: Context){
        this.context=context
    }

    override fun getInfoContents(marker: Marker): View {
       val view=LayoutInflater.from(context).inflate(R.layout.map_info_window,null)
        val mainDTO=marker.tag as MainDTO

        view.findViewById<TextView>(R.id.txt_value).text=mainDTO.scanText
        view.findViewById<TextView>(R.id.txt_location).text=mainDTO.locationName
        view.findViewById<TextView>(R.id.txt_floor).text=mainDTO.floorName
        view.findViewById<TextView>(R.id.txt_bay).text=mainDTO.bayNumber
        view.findViewById<TextView>(R.id.txt_operator).text=mainDTO.operatorName
        view.findViewById<TextView>(R.id.txt_timestamp).text= DateUtility.parseDateToDateTimeStr(Constant.COMBINE_DATE_TIME_FORMAT, Date(mainDTO.timestamp?:0))
        view.findViewById<TextView>(R.id.txt_compare).text=mainDTO.compareTimeFullStr
        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

}