package com.siliconstack.carscanner.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.*


import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.siliconstack.carscanner.model.FloorModel
import com.siliconstack.carscanner.model.MainModel

@Dao
interface FloorDAO {

    @Query("select * from FloorModel")
    fun getAll(): List<FloorModel>

    @Insert(onConflict = REPLACE)
    fun addRow(floorModel: FloorModel)

    @Delete
    fun deleteRow(floorModel: FloorModel)

    @Update
    fun updateRow(floorModel: FloorModel)


    @Query("delete from FloorModel where id =:id")
    fun deleteById(id: Int)

}