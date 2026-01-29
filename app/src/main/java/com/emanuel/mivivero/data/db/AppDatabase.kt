package com.emanuel.mivivero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emanuel.mivivero.data.local.dao.PlantaDao
import com.emanuel.mivivero.data.local.entity.PlantaEntity

@Database(
    entities = [PlantaEntity::class],
    version = 2   // 🔥 SUBIMOS LA VERSIÓN
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantaDao(): PlantaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mivivero_db"
                )
                    // 🔥 BORRA LA BASE VIEJA SI CAMBIA EL SCHEMA
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
