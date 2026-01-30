package com.emanuel.mivivero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.PlantaEntity

@Dao
interface PlantaDao {

    @Query("SELECT * FROM plantas")
    suspend fun getAll(): List<PlantaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planta: PlantaEntity)

    @Query("SELECT MAX(numeroPlanta) FROM plantas")
    suspend fun getMaxNumeroPlanta(): Int?

    @Query("DELETE FROM plantas WHERE id = :id")
    suspend fun deleteById(id: Long)

}
