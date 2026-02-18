package com.emanuel.mivivero.data.local.entity

import androidx.room.*

@Entity(
    tableName = "publicaciones_album",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("albumId")]
)
data class PublicacionAlbumEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val albumId: Long,

    val nombreVivero: String?,
    val fechaHasta: Long?,
    val modosPago: String?,
    val mediosEnvio: String?,
    val retiroEn: String?,
    val observaciones: String?,

    val fondoSeleccionado: Int
)
