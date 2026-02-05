package com.emanuel.mivivero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.FotoEntity



@Dao
interface FotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foto: FotoEntity)

    @Query("DELETE FROM fotos WHERE id = :fotoId")
    suspend fun deleteById(fotoId: Long)


    @Query("""
        SELECT * FROM fotos 
        WHERE plantaId = :plantaId 
        ORDER BY fecha DESC
    """)
    suspend fun getFotosPorPlanta(plantaId: Long): List<FotoEntity>

    @Delete
    suspend fun delete(foto: FotoEntity)

}
