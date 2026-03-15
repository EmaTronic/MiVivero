package com.emanuel.mivivero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.emanuel.mivivero.data.local.PlantaConLugar
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta

@Dao
interface PlantaDao {

    @Query("SELECT * FROM plantas")
    suspend fun getAll(): List<PlantaEntity>

    @Query(
        """
        SELECT plantas.*, lugares.icono AS lugarIcono
        FROM plantas
        LEFT JOIN lugares ON lugares.id = plantas.lugarId
        ORDER BY numeroPlanta ASC
        """
    )
    suspend fun getAllConLugar(): List<PlantaConLugar>

    @Query("SELECT * FROM plantas WHERE aLaVenta = 1")
    fun obtenerPlantasEnVenta(): LiveData<List<Planta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planta: PlantaEntity)

    @Update
    suspend fun update(planta: PlantaEntity)

    @Query("DELETE FROM plantas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT MAX(numeroPlanta) FROM plantas")
    suspend fun getMaxNumeroPlanta(): Int?

    @Query(
        """
        SELECT *
        FROM plantas
        WHERE aLaVenta = 1
          AND id NOT IN (
              SELECT plantaId
              FROM album_planta
              WHERE albumId = :albumId
          )
        ORDER BY numeroPlanta ASC
        """
    )
    suspend fun obtenerPlantasDisponiblesParaAlbum(albumId: Long): List<PlantaEntity>

    @Query("SELECT * FROM plantas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Planta?
}
