package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.model.RankingPlanta
import com.emanuel.mivivero.data.model.TotalPorAlbum

@Dao
interface VentaDao {

    @Insert
    suspend fun insert(venta: VentaEntity)

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun obtenerVentas(): LiveData<List<VentaEntity>>


    // 🔹 detalle de ventas con nombre de planta
    @Query("""
SELECT v.id, v.plantaId,
       p.familia || ' ' || IFNULL(p.especie, '') as nombrePlanta,
       v.cantidad, v.precioUnitario, v.fecha
FROM ventas v
LEFT JOIN plantas p ON p.id = v.plantaId
ORDER BY v.fecha DESC
""")
    fun obtenerVentasDetalle(): LiveData<List<VentaDetalle>>

    // 🔹 total por álbum
    @Query("""
        SELECT albumId, SUM(cantidad * precioUnitario) as total
        FROM ventas
        GROUP BY albumId
    """)
    fun obtenerTotalPorAlbum(): LiveData<List<TotalPorAlbum>>


    @Query("""
SELECT * FROM ventas 
WHERE plantaId = :plantaId 
AND albumId = :albumId
LIMIT 1
""")
    suspend fun obtenerVenta(plantaId: Long, albumId: Long): VentaEntity?


    // 🔹 ranking plantas
    @Query("""
        SELECT plantaId, SUM(cantidad) as totalVendidas
        FROM ventas
        GROUP BY plantaId
        ORDER BY totalVendidas DESC
    """)
    fun rankingPlantas(): LiveData<List<RankingPlanta>>



    // 🔹 ventas filtradas por mes/año
    @Query("""
SELECT * FROM ventas
WHERE strftime('%m', fecha / 1000, 'unixepoch') = :mes
AND strftime('%Y', fecha / 1000, 'unixepoch') = :anio
""")
    fun obtenerVentasPorMes(mes: String, anio: String): LiveData<List<VentaEntity>>



    @Query("""
SELECT 
    v.id AS id,
    v.plantaId AS plantaId,
    (p.familia || ' ' || IFNULL(p.especie, '')) AS nombrePlanta,
    v.cantidad AS cantidad,
    v.precioUnitario AS precioUnitario,
    v.fecha AS fecha
FROM ventas v
LEFT JOIN plantas p ON p.id = v.plantaId
ORDER BY v.fecha DESC
""")
    suspend fun obtenerVentasDetalleDirecto(): List<VentaDetalle>



    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    suspend fun debugVentas(): List<VentaEntity>


    // 🔹 eliminar
    @Delete
    suspend fun delete(venta: VentaEntity)

    // 🔹 actualizar
    @Update
    suspend fun update(venta: VentaEntity)
}