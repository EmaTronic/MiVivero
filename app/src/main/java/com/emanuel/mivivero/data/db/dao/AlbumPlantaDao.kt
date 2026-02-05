package com.emanuel.mivivero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity

@Dao
interface AlbumPlantaDao {

    @Insert
    suspend fun insert(relacion: AlbumPlantaEntity)
}
