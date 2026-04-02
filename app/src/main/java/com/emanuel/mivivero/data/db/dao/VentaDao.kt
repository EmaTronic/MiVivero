package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.model.RankingPlanta
import com.emanuel.mivivero.data.model.TotalPorAlbum

@Dao
interface VentaDao {

    @Insert
    suspend fun insert(venta: VentaEntity)

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun obtenerVentas(): LiveData<List<VentaEntity>>

    // 🔹 total por álbum
    @Query("""
        SELECT albumId, SUM(cantidad * precioUnitario) as total
        FROM ventas
        GROUP BY albumId
    """)
    fun obtenerTotalPorAlbum(): LiveData<List<TotalPorAlbum>>

    // 🔹 ranking plantas
    @Query("""
        SELECT plantaId, SUM(cantidad) as totalVendidas
        FROM ventas
        GROUP BY plantaId
        ORDER BY totalVendidas DESC
    """)
    fun rankingPlantas(): LiveData<List<RankingPlanta>>

    // 🔹 eliminar
    @Delete
    suspend fun delete(venta: VentaEntity)

    // 🔹 actualizar
    @Update
    suspend fun update(venta: VentaEntity)
}