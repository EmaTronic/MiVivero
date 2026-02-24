package com.emanuel.mivivero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.model.PlantaAlbum

@Dao
interface AlbumPlantaDao {

    // =========================
    // PLANTAS DEL ÁLBUM
    // =========================
    @Query("""
    SELECT 
        p.id AS plantaId,
        p.familia AS familia,
        p.especie AS especie,
        ap.cantidad AS cantidad,
        ap.precio AS precio,
        p.fotoRuta AS fotoRuta
    FROM plantas p
    INNER JOIN album_planta ap ON p.id = ap.plantaId
    WHERE ap.albumId = :albumId
""")
    fun obtenerPlantasDelAlbum(
        albumId: Long
    ): LiveData<List<PlantaAlbum>>

    // =========================
    // INSERTAR
    // =========================
    @Insert
    suspend fun insert(
        relacion: AlbumPlantaEntity
    )

    // =========================
    // 🔥 VERIFICAR DUPLICADO
    // =========================
    @Query("""
        SELECT COUNT(*) 
        FROM album_planta
        WHERE albumId = :albumId
          AND plantaId = :plantaId
    """)
    suspend fun existePlantaEnAlbum(
        albumId: Long,
        plantaId: Long
    ): Int

    // =========================
    // ELIMINAR PLANTA DEL ÁLBUM
    // =========================
    @Query("""
        DELETE FROM album_planta
        WHERE albumId = :albumId
          AND plantaId = :plantaId
    """)
    suspend fun eliminarPlantaDelAlbum(
        albumId: Long,
        plantaId: Long
    )

    // =========================
    // ACTUALIZAR CANTIDAD / PRECIO
    // =========================
    @Query("""
        UPDATE album_planta
        SET cantidad = :cantidad,
            precio = :precio
        WHERE albumId = :albumId
          AND plantaId = :plantaId
    """)
    suspend fun actualizarPlantaAlbum(
        albumId: Long,
        plantaId: Long,
        cantidad: Int,
        precio: Double
    )


    @Query("""
    SELECT 
        p.id AS plantaId,
        p.familia AS familia,
        p.especie AS especie,
        ap.cantidad AS cantidad,
        ap.precio AS precio,
        p.fotoRuta AS fotoRuta
    FROM album_planta ap
    INNER JOIN plantas p ON p.id = ap.plantaId
    WHERE ap.albumId = :albumId
""")
    suspend fun obtenerPlantasDelAlbumSuspend(
        albumId: Long
    ): List<PlantaAlbum>






    @Query("SELECT * FROM album_planta WHERE albumId = :albumId")
    suspend fun obtenerPlantasDelAlbumRaw(albumId: Long): List<AlbumPlantaEntity>



}
