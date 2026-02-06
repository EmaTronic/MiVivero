package com.emanuel.mivivero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emanuel.mivivero.data.local.entity.AlbumPlantaEntity
import com.emanuel.mivivero.data.model.PlantaAlbum

@Dao
interface AlbumPlantaDao {

    @Query("""
        SELECT 
            p.id AS idPlanta,
            (p.familia || 
                CASE 
                    WHEN p.especie IS NOT NULL THEN ' ' || p.especie 
                    ELSE '' 
                END
            ) AS nombre,
            ap.cantidad AS cantidad,
            ap.precio AS precio,
            p.fotoRuta AS fotoRuta
        FROM plantas p
        INNER JOIN album_planta ap ON p.id = ap.plantaId
        WHERE ap.albumId = :albumId
    """)
    fun obtenerPlantasDelAlbum(albumId: Long): LiveData<List<PlantaAlbum>>

    @Insert
    suspend fun insert(relacion: AlbumPlantaEntity)
}
