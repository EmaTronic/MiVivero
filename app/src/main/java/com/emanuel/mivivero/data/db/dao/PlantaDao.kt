package com.emanuel.mivivero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta

@Dao
interface PlantaDao {

    @Query("SELECT * FROM plantas")
    suspend fun getAll(): List<PlantaEntity>

    @Query("SELECT * FROM plantas WHERE aLaVenta = 1")
    fun obtenerPlantasEnVenta(): LiveData<List<Planta>>



    // ✅ INSERTA ENTITY, NO MODEL
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planta: PlantaEntity)

    @Update
    suspend fun update(planta: PlantaEntity)

    @Query("DELETE FROM plantas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT MAX(numeroPlanta) FROM plantas")
    suspend fun getMaxNumeroPlanta(): Int?
}
