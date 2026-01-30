package com.emanuel.mivivero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = PlantaEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantaId")]
)
data class FotoEntity(

    @PrimaryKey
    val id: Long,

    val plantaId: Long,

    val ruta: String,

    val fecha: Long,          // 🔥 ESTA COLUMNA DEBE EXISTIR

    val observaciones: String?
)
