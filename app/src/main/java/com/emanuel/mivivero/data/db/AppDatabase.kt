package com.emanuel.mivivero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emanuel.mivivero.data.db.dao.AlbumDao
import com.emanuel.mivivero.data.db.dao.PublicacionAlbumDao
import com.emanuel.mivivero.data.db.dao.UsuarioDao
import com.emanuel.mivivero.data.db.entity.UsuarioEntity
import com.emanuel.mivivero.data.local.dao.AlbumPlantaDao

import com.emanuel.mivivero.data.local.dao.FotoDao
import com.emanuel.mivivero.data.local.dao.PlantaDao
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.local.entity.FotoEntity
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.local.entity.PublicacionAlbumEntity

@Database(
    entities = [
        PlantaEntity::class,
        AlbumEntity::class,
        AlbumPlantaEntity::class,
        FotoEntity::class,
        UsuarioEntity::class,
        PublicacionAlbumEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plantaDao(): PlantaDao
    abstract fun fotoDao(): FotoDao
    abstract fun albumDao(): AlbumDao
    abstract fun albumPlantaDao(): AlbumPlantaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun publicacionAlbumDao(): PublicacionAlbumDao



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
