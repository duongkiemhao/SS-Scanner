package com.siliconstack.carscanner.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.siliconstack.carscanner.dao.FloorDAO
import com.siliconstack.carscanner.dao.LocationDAO

import com.siliconstack.carscanner.dao.MainDAO
import com.siliconstack.carscanner.dao.NameDAO
import com.siliconstack.carscanner.model.*

@Database(entities = arrayOf(MainModel::class,LocationModel::class,FloorModel::class,OperatorModel::class), version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mainDAO(): MainDAO
    abstract fun locationDAO(): LocationDAO
    abstract fun floorDAO(): FloorDAO
    abstract fun nameDAO(): NameDAO

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                val MIGRATION_2_3 = object : Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE MainModel ADD COLUMN lat REAL DEFAULT 0.0")
                        database.execSQL("ALTER TABLE MainModel ADD COLUMN lng REAL DEFAULT 0.0")
                        database.execSQL("ALTER TABLE MainModel ADD COLUMN image TEXT")
                    }
                }


                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "main")
                        .addMigrations(MIGRATION_2_3)
                        .allowMainThreadQueries().build()
            }
            return INSTANCE as AppDatabase
        }
    }

}