package com.emanuel.mivivero.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.PublicacionAlbumEntity

@Dao
interface PublicacionAlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(publicacion: PublicacionAlbumEntity)

    @Query("SELECT * FROM publicaciones_album WHERE albumId = :albumId LIMIT 1")
    suspend fun obtenerPorAlbum(albumId: Long): PublicacionAlbumEntity?
}
