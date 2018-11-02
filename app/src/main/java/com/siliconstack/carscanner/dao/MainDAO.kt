package com.siliconstack.carscanner.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.*


import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.siliconstack.carscanner.model.MainDTO
import com.siliconstack.carscanner.model.MainModel

@Dao
interface MainDAO {

    @RawQuery
    fun query(query:SupportSQLiteQuery): List<MainDTO>

    @Insert(onConflict = REPLACE)
    fun addRow(mainModel: MainModel)

    @Delete
    fun deleteRow(mainModel: MainModel)

    @Update
    fun updateRow(mainModel: MainModel)


    @Query("delete from MainModel where id =:id")
    fun deleteById(id: Int)

    @Query("delete from MainModel where id IN(:ids)")
    fun deleteByIds(ids: IntArray)


}