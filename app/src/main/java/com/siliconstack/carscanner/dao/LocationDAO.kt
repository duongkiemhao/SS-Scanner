package com.siliconstack.carscanner.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.*


import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.siliconstack.carscanner.model.FloorModel
import com.siliconstack.carscanner.model.LocationModel
import com.siliconstack.carscanner.model.MainModel

@Dao
interface LocationDAO {

    @Query("select * from LocationModel")
    fun getAll(): List<LocationModel>

    @Insert(onConflict = REPLACE)
    fun addRow(locationModel: LocationModel)

    @Delete
    fun deleteRow(locationModel: LocationModel)

    @Query("delete from LocationModel where id =:id")
    fun deleteById(id: Int)

    @Update
    fun updateRow(locationModel: LocationModel)



}