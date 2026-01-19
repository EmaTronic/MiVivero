package com.emanuel.mivivero.data.local.database

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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantaDao(): PlantaDao
    abstract fun fotoDao(): FotoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mivivero_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
