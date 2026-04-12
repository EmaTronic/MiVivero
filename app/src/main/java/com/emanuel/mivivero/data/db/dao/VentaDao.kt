package com.emanuel.mivivero.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.model.AlbumResumen
import com.emanuel.mivivero.data.model.ControlStock
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.data.model.PlantaSimple
import com.emanuel.mivivero.data.model.RankingPlanta
import com.emanuel.mivivero.data.model.ResumenPlanta
import com.emanuel.mivivero.data.model.TotalAlbum
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
    SELECT 
        p.id as plantaId,
        p.familia || ' ' || IFNULL(p.especie, '') as nombrePlanta,
        SUM(v.cantidad) as totalVendidas
    FROM ventas v
    INNER JOIN plantas p ON p.id = v.plantaId
    GROUP BY v.plantaId
    ORDER BY totalVendidas DESC
    """)
    fun rankingPlantas(): LiveData<List<RankingPlanta>>

    @Query("""
    SELECT SUM(cantidad * precioUnitario)
    FROM ventas
    WHERE albumId = :albumId
    """)
    fun totalPorAlbum(albumId: Long): LiveData<Double>

    // 🔹 ventas filtradas por mes/año
    @Query("""
    SELECT * FROM ventas
    WHERE strftime('%m', fecha / 1000, 'unixepoch') = :mes
    AND strftime('%Y', fecha / 1000, 'unixepoch') = :anio
    """)
    fun obtenerVentasPorMes(mes: String, anio: String): LiveData<List<VentaEntity>>


    @Query("""
    SELECT SUM(cantidad * precioUnitario) FROM ventas
    """)
    fun obtenerTotalGeneral(): LiveData<Double>

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




    @Query("""
    SELECT v.id, v.plantaId,
           (p.familia || ' ' || IFNULL(p.especie, '')) as nombrePlanta,
           v.cantidad, v.precioUnitario, v.fecha,
           v.albumId
    FROM ventas v
    LEFT JOIN plantas p ON p.id = v.plantaId
    WHERE v.albumId = :albumId
    ORDER BY v.fecha DESC
    """)
    fun obtenerVentasPorAlbum(albumId: Long): LiveData<List<VentaDetalle>>





    @Query("""
    SELECT 
        a.id as albumId,
        a.nombre as nombre,
        IFNULL(SUM(v.cantidad * v.precioUnitario), 0) as totalGanado,
        IFNULL(SUM(v.cantidad), 0) as totalVendidas
    FROM albumes a
    LEFT JOIN ventas v ON v.albumId = a.id
    GROUP BY a.id
    ORDER BY a.fechaCreacion DESC
    """)
    fun obtenerResumenAlbumes(): LiveData<List<AlbumResumen>>

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    suspend fun debugVentas(): List<VentaEntity>


    //BORRAR VENTA DE DETALLE ALBUM
    @Query("DELETE FROM ventas WHERE id = :id")
    suspend fun deleteById(id: Long)

    //EDITAR VENTA DE DETALLE ALBUM
    @Query("""
    UPDATE ventas 
    SET cantidad = :cantidad,
        precioUnitario = :precio,
        fecha = :fecha
    WHERE id = :id
    """)

    suspend fun updateVentaCompleta(
        id: Long,
        cantidad: Int,
        precio: Double,
        fecha: Long
    )

    @Query("""
    SELECT 
        p.id as plantaId,
        p.familia as familia,
        p.especie as especie,
        (p.familia || ' ' || IFNULL(p.especie, '')) as nombreCompleto,
        ap.cantidad as cantidad,
        ap.precio as precio,
        p.fotoRuta as fotoRuta
    FROM plantas p
    INNER JOIN album_planta ap ON ap.plantaId = p.id
    WHERE ap.albumId = :albumId
    """)
    fun obtenerPlantasDelAlbum(albumId: Long): LiveData<List<PlantaAlbum>>


    //TRAER LA LISTA DE PLANTAS QUE NO HAN SIDO VENDIDAS
    @Query("""
    SELECT p.id, p.familia || ' ' || IFNULL(p.especie, '') as nombre
    FROM plantas p
    INNER JOIN album_planta ap ON ap.plantaId = p.id
    WHERE ap.albumId = :albumId
    ORDER BY p.familia, p.especie
    """)
    suspend fun plantasDisponiblesParaVenta(albumId: Long): List<PlantaSimple>

//TOTAL DE PLANTAS VENDIDAS

    @Query("""
    SELECT 
        p.familia || ' ' || IFNULL(p.especie, '') AS nombrePlanta,
        SUM(v.cantidad) as totalVendidas
    FROM ventas v
    INNER JOIN plantas p ON p.id = v.plantaId
    WHERE v.albumId = :albumId
    GROUP BY v.plantaId
    ORDER BY totalVendidas DESC
    """)
    fun resumenPorPlanta(albumId: Long): LiveData<List<ResumenPlanta>>

    //AVISO DE STOCK NEGATIVO

    @Query("""
    SELECT 
        p.id as plantaId,
        p.familia || ' ' || IFNULL(p.especie, '') AS nombrePlanta,
        p.cantidad as stockActual,
        IFNULL(SUM(v.cantidad), 0) as vendidas
    FROM plantas p
    LEFT JOIN ventas v ON v.plantaId = p.id AND v.albumId = :albumId
    GROUP BY p.id
    """)
    fun controlStock(albumId: Long): LiveData<List<ControlStock>>


    @Query("""
SELECT SUM(cantidad * precioUnitario) FROM ventas
""")
    fun totalGeneral(): LiveData<Double?>


    @Query("""
SELECT 
    albumId,
    SUM(cantidad * precioUnitario) as totalGanado
FROM ventas
GROUP BY albumId
ORDER BY totalGanado DESC
""")
    fun totalPorAlbum(): LiveData<List<TotalAlbum>>


    // 🔹 eliminar
    @Delete
    suspend fun delete(venta: VentaEntity)

    // 🔹 actualizar
    @Update
    suspend fun update(venta: VentaEntity)
}