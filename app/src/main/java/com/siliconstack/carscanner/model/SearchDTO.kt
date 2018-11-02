package com.siliconstack.carscanner.model

import android.os.Parcel
import android.os.Parcelable
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class SearchDTO(title: String, items: List<MainDTO>) : ExpandableGroup<MainDTO>(title, items)