package com.emanuel.mivivero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.FotoEntity

@Dao
interface FotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foto: FotoEntity)

    @Query("""
        SELECT * FROM fotos 
        WHERE plantaId = :plantaId 
        ORDER BY fecha DESC
    """)
    suspend fun getFotosPorPlanta(plantaId: Long): List<FotoEntity>
}
