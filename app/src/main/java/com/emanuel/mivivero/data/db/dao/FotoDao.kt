package com.emanuel.mivivero.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.db.entity.FotoEntity

@Dao
interface FotoDao {

    @Query("SELECT * FROM fotos WHERE plantaId = :plantaId")
    suspend fun getFotosDePlanta(plantaId: Long): List<FotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foto: FotoEntity)

    @Delete
    suspend fun delete(foto: FotoEntity)

    @Query("UPDATE fotos SET esPrincipal = 0 WHERE plantaId = :plantaId")
    suspend fun limpiarPrincipal(plantaId: Long)

    @Query("UPDATE fotos SET esPrincipal = 1 WHERE id = :fotoId")
    suspend fun marcarPrincipal(fotoId: Long)
}
