package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.AlbumEntity

@Dao
interface AlbumDao {

    @Insert
    suspend fun insert(album: AlbumEntity): Long

    @Query("SELECT * FROM albumes")
    suspend fun getAll(): List<AlbumEntity>

    @Query("SELECT * FROM albumes ORDER BY fechaCreacion DESC")
    fun getAlbumes(): LiveData<List<AlbumEntity>>




}
