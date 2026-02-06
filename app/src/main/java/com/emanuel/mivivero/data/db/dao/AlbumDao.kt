package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.AlbumEntity

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albumes WHERE id = :albumId")
    fun obtenerPorId(albumId: Long): LiveData<AlbumEntity?>

    @Query("SELECT * FROM albumes ORDER BY fechaCreacion DESC")
    fun getAlbumes(): LiveData<List<AlbumEntity>>

    @Insert
    suspend fun insert(album: AlbumEntity): Long

    @Query("UPDATE albumes SET estado = :estado WHERE id = :albumId")
    suspend fun actualizarEstado(albumId: Long, estado: String)
}

