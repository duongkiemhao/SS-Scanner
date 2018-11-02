package com.siliconstack.carscanner.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.siliconstack.carscanner.dao.FloorDAO
import com.siliconstack.carscanner.dao.LocationDAO

import com.siliconstack.carscanner.dao.MainDAO
import com.siliconstack.carscanner.dao.NameDAO
import com.siliconstack.carscanner.model.FloorModel
import com.siliconstack.carscanner.model.LocationModel
import com.siliconstack.carscanner.model.MainModel
import com.siliconstack.carscanner.model.OperatorModel

@Database(entities = arrayOf(MainModel::class,LocationModel::class,FloorModel::class,OperatorModel::class), version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mainDAO(): MainDAO
    abstract fun locationDAO(): LocationDAO
    abstract fun floorDAO(): FloorDAO
    abstract fun nameDAO(): NameDAO

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "main")
                        .allowMainThreadQueries().build()
            }
            return INSTANCE as AppDatabase
        }
    }

}