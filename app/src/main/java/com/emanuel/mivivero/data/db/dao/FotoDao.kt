package com.emanuel.mivivero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.FotoEntity

@Dao
interface FotoDao {

    @Query("SELECT * FROM fotos WHERE plantaId = :plantaId")
    suspend fun getFotosByPlanta(plantaId: Int): List<FotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foto: FotoEntity)
}
