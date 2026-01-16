package com.emanuel.mivivero.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emanuel.mivivero.data.db.dao.FotoDao
import com.emanuel.mivivero.data.db.dao.PlantaDao
import com.emanuel.mivivero.data.db.entity.FotoEntity
import com.emanuel.mivivero.data.db.entity.PlantaEntity

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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mivivero.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
