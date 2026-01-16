package com.emanuel.mivivero.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emanuel.mivivero.data.db.entity.PlantaEntity

@Dao
interface PlantaDao {

    @Query("SELECT * FROM plantas")
    suspend fun getPlantas(): List<PlantaEntity>

    @Query("SELECT * FROM plantas WHERE id = :id")
    suspend fun getPlantaById(id: Long): PlantaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planta: PlantaEntity)
}
