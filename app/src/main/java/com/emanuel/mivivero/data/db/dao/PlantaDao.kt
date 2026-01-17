package com.emanuel.mivivero.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.emanuel.mivivero.data.db.entity.PlantaEntity
import com.emanuel.mivivero.data.model.PlantaConFoto
import kotlinx.coroutines.flow.Flow
import androidx.room.Insert
import androidx.room.OnConflictStrategy


@Dao
interface PlantaDao {

    @Query("SELECT * FROM plantas ORDER BY numeroPlanta")
    fun getPlantasFlow(): Flow<List<PlantaEntity>>

    @Query("SELECT * FROM plantas WHERE id = :id")
    suspend fun getPlantaById(id: Long): PlantaEntity?

    @Transaction
    @Query("SELECT * FROM plantas ORDER BY numeroPlanta")
    fun getPlantasConFoto(): Flow<List<PlantaConFoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planta: PlantaEntity)

}
