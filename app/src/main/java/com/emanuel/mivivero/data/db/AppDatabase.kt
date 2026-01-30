package com.emanuel.mivivero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emanuel.mivivero.data.local.dao.FotoDao
import com.emanuel.mivivero.data.local.dao.PlantaDao
import com.emanuel.mivivero.data.local.entity.FotoEntity
import com.emanuel.mivivero.data.local.entity.PlantaEntity

@Database(
    entities = [
        PlantaEntity::class,
        FotoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantaDao(): PlantaDao
    abstract fun fotoDao(): FotoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vivero.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
