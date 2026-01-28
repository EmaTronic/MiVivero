package com.emanuel.mivivero.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emanuel.mivivero.data.local.dao.PlantaDao
import com.emanuel.mivivero.data.local.entity.PlantaEntity

@Database(
    entities = [PlantaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantaDao(): PlantaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mivivero_db"
                ).build().also { INSTANCE = it }
            }
    }
}
